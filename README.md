# browser

A Clojure library for dynamic browsing with cookies for authentication navigation
## Usage

[![Clojars Project](http://clojars.org/org.clojars.kokos/browser/latest-version.svg)](http://clojars.org/org.clojars.kokos/browser)

```(use 'browser.core)

(def client (create))

(with-client client
  (doget "https://github.com/kokosro/browse"))```

will produce a response map


## License



Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
