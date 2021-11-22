(ns booksclubwithauth.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[booksclubwithauth started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[booksclubwithauth has shut down successfully]=-"))
   :middleware identity})
