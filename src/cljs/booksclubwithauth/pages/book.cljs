(ns booksclubwithauth.pages.book
  (:require
   [re-frame.core :as rf]
   [fork.re-frame :as fork]
   [booksclubwithauth.pages.error :refer [error]]
   [booksclubwithauth.validation :refer [add-update-book-validation]]
   ["react-toastify" :refer [ToastContainer toast]]))

(defn book [id]
  (let [book @(rf/subscribe [:books/read-book id])]
   [:div.card
    [:div.card-image.has-text-centered
     [:figure.image.is-64x64.is-inline-block
      [:img {:height 200
             :width 200
             :src (if (empty? (:image_url book))
                    "https://st3.depositphotos.com/23594922/31822/v/600/depositphotos_318221368-stock-illustration-missing-picture-page-for-website.jpg"
                    (:image_url book))}]]]
    [:div.card-content
     [:div.media
      [:div.media-content
       [:p.title.is-5 (:title book)]
       [:p.subtitle.is-7 (:author book)]]]]
    [:footer.card-footer
     [:a.card-footer-item {:href (str "#/book/" id "/edit")} "Update"]
     [:a.card-footer-item {:on-click (fn [e]
                                       (.preventDefault e)
                                       (rf/dispatch [:delete-book id]))}  "Delete"]]]))

(defn my-books []
  (let [loading @(rf/subscribe [:loading/my-books])
        book-ids @(rf/subscribe [:books/my-read-books-ids])]
    (if loading
      [:div.container.mt-5
       [:p "LOADING..."]]
      [:div.container.mt-5
       [:div.columns
        [:p.title.is-4.has-text-centered
         "Your Books"]]
       ;[:> ToastContainer
       ; {:position "top-center"
       ;  :autoClose 5000}]
       ;[:button {:on-click (fn [e]
       ;                      (.preventDefault e)
       ;                      (toast "sadf" {:position "top-center"
       ;                                     :autoClose 5000}))} "CLICk"]
       [:a.button.is-link {:href "#/book/add"} "Add New Book"]
       (if (empty? book-ids)
         [:div.columns.is-mobile.mt-5
          [:p.title.is-4.has-text-centered
           "No Books..."]]
         [:div.columns.is-mobile
          (for [id book-ids]
            [:div.column.is-4 {:key id}
             [book id]])])])))

(defn add-update-book [add? & rs]
  (let [errors @(rf/subscribe [:error])]
    [:div.container
     [:div.columns
      [:div.column.is-12.has-text-centered
       [:p.title.is-3
        (if add?
          "Add New Book"
          "Update Book")]]]
     (if errors
       [:div
        [error errors]])
     [:div.columns
      [:div.column.is-4.is-offset-4
       [fork/form
        {:initial-values (first rs)
         :validation add-update-book-validation
         :keywordize-keys true
         :on-submit (fn [state]
                      (if add?
                        (rf/dispatch [:add-book (:values state)])
                        (rf/dispatch [:update-book (:values state) (second rs)]))),
         :prevent-default? true}
        (fn [{:keys
              [values
               touched
               errors
               handle-change
               handle-blur
               handle-submit
               attempted-submissions]}]
          (let [render-error (fn [x]
                               (when (and (touched x) (get errors x))
                                 [:div.has-text-danger (get errors x)]))]
            [:form
             {:on-submit handle-submit}
             [:div.field
              [:label.label "Title"]
              [:div.control
               [:input.input {:type :text,
                              :name :title,
                              :value (:title values),
                              :on-change handle-change,
                              :on-blur handle-blur
                              :placeholder "Enter book title"}]]
              [render-error :title]]

             [:div.field
              [:label.label "Author"]
              [:div.control
               [:input.input {:type :text,
                              :name :author,
                              :value (:author values),
                              :on-change handle-change,
                              :on-blur handle-blur
                              :placeholder "Enter book author/authors"}]]
              [render-error :author]]

             [:div.field
              [:label.label "Book Image URL"]
              [:div.control
               [:input.input {:type :text,
                              :name :image_url,
                              :value (:image_url values),
                              :on-change handle-change,
                              :on-blur handle-blur
                              :placeholder "Enter book image url"}]]
              [render-error :image_url]]

             [:div.field
              [:label.label "Description"]
              [:div.control
               [:textarea.textarea {:name :description,
                                    :value (:description values),
                                    :on-change handle-change,
                                    :on-blur handle-blur
                                    :placeholder "Enter description"}]]
              [render-error :description]]

             [:div.field
              [:label.label "Rating"]
              [:div.control
               [:input.input {:type :number,
                              :name :rating,
                              :value (:rating values),
                              :on-change handle-change,
                              :on-blur handle-blur
                              :placeholder "Enter rating out of 10"}]]
              [render-error :rating]]
             [:div.field.has-text-centered.mt-4
              [:div.control
               [:button.button.is-link
                {:disabled (and (seq errors) (> attempted-submissions 0))}
                (if add?
                  "Add"
                  "Update")]]]
             ]))]]]]))

(defn add-book
  []
  [:div
   [add-update-book true {:title ""
                          :author ""
                          :image_url ""
                          :description ""
                          :rating ""}]])

(defn update-book
  []
  (let [book-data @(rf/subscribe [:update-book])]
    [:div
     (if book-data
       [add-update-book false book-data (:id book-data)])]))