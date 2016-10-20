(ns browser.belt
  (:require [uri.core :as u]
            [clojure.string :as s])
  (:gen-class))


(defn url-info [url]
  (u/uri->map (u/make url)))

(defn host [url]
  (:host (url-info url)))

(defn hosts [& urls]
  (map host urls))


(defn empty-response [url]
  {:url url
   :url-info (url-info url)
   :status 200
   :redirects []
   :content-type "text/html"
   :content-type-info "charset=UTF-8"
   :response-time 0
   :content-size 0
   :error 0
   :body ""})

(defn fetchable? [url & {:keys [accepted-schemes] 
                             :or {accepted-schemes ["http" "https"]}}]
  (if (and 
        (not (= "" url))
        (not (nil? url))
        (u/absolute? (u/make url)))
    (let [url-map (u/uri->map (u/make url))]
      (reduce (fn [r x]
                (or r
                    (= x (:scheme url-map)))) false accepted-schemes))
    false))

(defn no-fragments [url]
  (if-not (nil? url)
    (first (s/split url #"#"))
    nil))

(defn normalize-url [base-url url]
  (if-not (nil? url)
    (try
      (let [x (no-fragments (.toString (u/resolve (u/make base-url) 
                                                  (u/make (s/replace url #" " "")))))]
        (if (fetchable? x)
          x
          base-url))
      (catch Exception e base-url))
    base-url))

(defn =host? [base-host & urls]
  (reduce #(let [url-info (u/uri->map (u/make %2))]
             (and %1 (or (= base-host (:host url-info))
                         (= base-host (s/replace (:host url-info) #"www\." ""))))) true urls))


(defn not-a-valid-url-response [url]
  {:body ""
   :error 600
   :status 0
   :content-type ""
   :content-type-info ""
   :response-time ""
   :content-size 0
   :redirects []
   :url url})