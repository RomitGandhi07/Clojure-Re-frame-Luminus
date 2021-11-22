(ns booksclubwithauth.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [booksclubwithauth.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[booksclubwithauth started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[booksclubwithauth has shut down successfully]=-"))
   :middleware wrap-dev})
