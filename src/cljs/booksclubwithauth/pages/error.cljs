(ns booksclubwithauth.pages.error)

(defn error
  [error]
  (cond
    (string? error)
    [:div.column.is-4.is-offset-4
     [:p {:style {:color :red}} error]]
    (map? error)
    [:div.column.is-4.is-offset-4
     (for [[key e] error]
       ^{:key (gensym)} [:p {:style {:color :red}} e])]
    :else
    nil))