(ns booksclubwithauth.pages.user
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]))


(defn search-users
  []
  (let [state (r/atom {})]
    [:<>
     [:input.input
     {:type :text
      :name :search
      :on-change (fn [e]
                   (swap! state assoc :value (-> e .-target .-value)))}]
     [:button.button.is-link
      {:on-click (fn [e]
                   (.preventDefault e)
                   (rf/dispatch [:search-users]))}
      "Search"]]))

;(defn search-users
;  []
;  [:<>
;   [:> (aget js/ReactToastify "ToastContainer")]
;   [:button {:on-click #((aget js/ReactToastify "toast") "My Toast")} "Example"] ])