(ns booksclubwithauth.pages.user
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]))

(defn user
  [u]
  [:div
   (when-not (empty? (:profile_pic u))
     [:img.image.is-64x64.is-rounded {:src (:profile_pic u)}])
   [:p (:name u)]])

(defn users-list
  []
  (let [searched-users @(rf/subscribe [:searched-users])]
    [:div.columns.is-mobile.mt-5
     (for [u searched-users]
       [:div {:key (:id u)}
        [user u]])]))

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
                   (rf/dispatch [:search-users (:value @state)]))}
      "Search"]
     [users-list]]))