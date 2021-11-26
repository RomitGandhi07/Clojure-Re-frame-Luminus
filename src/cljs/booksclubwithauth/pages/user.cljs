(ns booksclubwithauth.pages.user
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]))

(defn user
  [id]
  (let [user @(rf/subscribe [:searched-user-info id])
        followed @(rf/subscribe [:searched-user-info-follow id])]
    [:div.column
     [:div.card
      [:div.card-image.has-text-centered
       [:figure.image.is-64x64.is-inline-block
        [:img {:src (if-not (empty? (:profile_pic user))
                      (:profile_pic user)
                      "https://t3.ftcdn.net/jpg/03/46/83/96/360_F_346839683_6nAPzbhpSkIpb8pmAwufkC7c5eD7wYws.jpg")}]]]
      [:div.card-content
       [:div.media
        [:div.media-content
         [:p.title.is-5 (:name user)]]]]
      [:footer.card-footer
       (if followed
         [:a.card-footer-item {:on-click (fn [e]
                                           (.preventDefault e)
                                           (rf/dispatch [:unfollow-user (:id user)]))} "Following"]
         [:a.card-footer-item {:on-click (fn [e]
                                           (.preventDefault e)
                                           (rf/dispatch [:follow-user (:id user)]))} "Follow"])]]]))

(defn users-list
  []
  (let [ids @(rf/subscribe [:searched-users-ids])]
    (if-not (empty? ids)
      [:div.columns.is-mobile.mt-5
       (doall
         (for [id ids]
           [:div {:key id}
           [user id]]))]
      nil)))

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