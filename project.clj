(defproject org.clojars.kokos/browser "0.2.2c-2a"
  :description "Basic HTTP client utility. With cookie store for authenticated navigation. "
  :url "https://github.com/kokosro/browser"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo/"
                   :sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;[clj-http "2.3.0"]
                 [clj-http "3.3.0"] ;;testing latest version of clj-http, maybe nothing has to change in my code :D
                 [uri "1.1.0"]
                 [clj-time "0.8.0"]
                 [slingshot "0.12.2"]])
