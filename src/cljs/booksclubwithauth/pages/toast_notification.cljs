(ns booksclubwithauth.pages.toast-notification
  (:require
    [re-frame.core :as rf]
    ["react-toastify" :refer [ToastContainer toast]]))

(defn toast-notification
  []
  (let [toast-notification @(rf/subscribe [:toast])]
    (if toast-notification
      [:<>
       (cond
         (contains? toast-notification :success)
         (.success toast (:success toast-notification))
         (contains? toast-notification :error)
         (.error toast (:error toast-notification))
         (contains? toast-notification :info)
         (.info toast (:info toast-notification))
         (contains? toast-notification :warn)
         (.warn toast (:warn toast-notification))
         :else
         (toast (:normal toast-notification)))
       (rf/dispatch [:clear-toast-notification])]
      nil)))
