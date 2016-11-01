(ns browser.core
  (:require [clj-http.cookies]
            [clj-http.client :as client]
            [uri.core :as u]
            [clojure.string :as s]
            [browser.header :as header]
            [browser.belt :as belt])
  (:use [slingshot.slingshot :only [try+]])
  (:gen-class))

;;each tab is an agent with private scope

(defn- build-response 
  [url response]
  {:url url
   :url-info (u/uri->map (u/make url))
   :status (:status response)
   :redirects (:trace-redirects response)
   :content-type (header/extract-name (:headers response) "Content-Type")
   :content-type-info (header/extract-info (:headers response) "Content-Type")
   :response-time (:request-time response)
   :content-size (header/extract-value (:headers response) "Content-Length")
   :error 0
   :body (:body response)})

(defn- request-handler [fetching-fn args]
  (let [fetched-response (try+ (let [response (fetching-fn args)] response)
                               (catch Object _ _))]
    (build-response (:url args) 
                    fetched-response)))

(defn- merge-default-headers [to]
  (merge {"User-Agent" "Basic Browker"} 
         to))

(defn- build-simple-request [url method headers]
  {:method method
   :url url
   :insecure? true
   :follow-redirects true
   :max-redirects 10
   :socket-timeout 2000 :conn-timeout 2000
   :headers (merge-default-headers headers)})

(defn- new-state
  [scope]
  {:scope scope
   :history []
   :current {:url nil
             :method :get
             :params nil
             :response (belt/empty-response "")}})

(defn- execute-request [& {:keys [url scope  method params headers]
                           :or {method :get
                                params nil
                                scope (clj-http.cookies/cookie-store)
                                headers {}}}]
  (if (belt/fetchable? url)
    (request-handler client/request (merge 
                                      {:cookie-store scope}
                                      (build-simple-request url method headers)
                                      (if-not (nil? params)
                                        (if (= :get method)
                                          {:query-params params}
                                          (if (map? params)
                                            {:form-params params}
                                            {:body params})))))
    (belt/not-a-valid-url-response url)))

(defn- selected-option-value 
  [options]
  (reduce (fn [v option]
            (if (= "selected" (or (-> option :option :value :selected)
                                  ""))
              (-> option :option :value :value)))
          (-> (first options)
              :option
              :value
              :value)
          options))

(defn- merge-form-element
  [data element]
  (let [field-name (case (first element)
                     :select (-> element :select :value :name)
                     (-> element :input :value :name))]
    (if (and (not (nil? field-name))
             (not (= "" (s/trim field-name))))
      (merge (assoc {}
               (keyword field-name)
               (case (first element)
                 :select (selected-option-value (-> element :select :content))
                 (-> element :input :value :value)))
             data)
      data)))

(defn- construct-form-data
  [form data]
  (reduce (fn [data element]
            (merge-form-element data element))
          data
          (-> form :form :content)))

(defn navigate 
  ([state]
   (navigate state (:url (:current state))
             (:method (:current state))
             (:params (:current state))))
  ([state to-url]
   (navigate state to-url :get))
  ([state to-url method]
   (navigate state to-url method nil))
  ([state to-url method params]
   (let [history (concat (:history state) 
                         [(:current state)])
         url (if (nil? (-> state :current :url))
               to-url
               (belt/normalize-url (-> state
                                       :current
                                       :url) 
                                   to-url))
         response (try
                    (execute-request
                      :url url
                      :method method
                      :params params
                      :scope (:scope state))
                    (catch Exception e (do (println e)
                                         {:error e})))]
     {:scope (:scope state)
      :history history
      :current {:url url
                :method method
                :params params
                :response response}})))

(defn submit-form
  [state form data]
  (let [action-url (belt/normalize-url
                     (-> state :current :url)
                     (-> form :form :value :action))
        form-data (construct-form-data form data)
        method (keyword (s/lower-case 
                          (or (-> form :form :value :method)
                              "GET")))]
    (navigate state action-url method form-data)))

(defn follow-link
  [state link params]
  (let [action-url (belt/normalize-url
                     (-> state :current :url)
                     (-> link
                         :url
                         :value
                         :link))]
    (navigate state action-url :get params)))

(defn open 
  ([]
   (open (new-state (clj-http.cookies/cookie-store))))
  ([state]
   (fn [& action]
     (if (nil? (first action))
       state
       (let [page (promise)
             ;;no more future for the time being, just return, don't reliver
             _ 0]
         (apply (first action)
                (into [state]
                      (rest action)))
         ;page
         )))))



