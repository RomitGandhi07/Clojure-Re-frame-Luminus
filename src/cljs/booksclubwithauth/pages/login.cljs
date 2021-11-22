(ns booksclubwithauth.pages.login
  (:require
    [re-frame.core :as rf]
    [fork.re-frame :as fork]
    [booksclubwithauth.pages.error :refer [error]]
    [booksclubwithauth.validation :refer [validate-user-login-data]]))

(defn login []
  ()
   (let [errors @(rf/subscribe [:error])]
    [:div.container
     [:div.columns
      [:div.column.is-12.has-text-centered
       [:p.title.is-3 "Login"]]]
     (if errors
       [:div
        [error errors]])
      [:div.column.is-4.is-offset-4
       [fork/form
        {:keywordize-keys  true
         :validation       validate-user-login-data,
         :on-submit        (fn [state]
                             (rf/dispatch [:user-login (:values state)])),
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
              [:label.label "Email"]
              [:div.control
               [:input.input {:type        :email,
                              :name        :email,
                              :value       (:email values),
                              :on-change   handle-change,
                              :on-blur     handle-blur
                              :placeholder "Enter your email Eg: abc@xyz.com"}]]
              [render-error :email]]

             [:div.field
              [:label.label "Password"]
              [:div.control
               [:input.input {:type        :password,
                              :name        :password,
                              :value       (:password values),
                              :on-change   handle-change,
                              :on-blur     handle-blur
                              :placeholder "Enter your password"}]]
              [render-error :password]]
             [:div.field.has-text-centered.mt-4
              [:div.control
               [:button.button.is-link
                {:disabled (and (seq errors) (> attempted-submissions 0))}
                "Submit"]]]
             [:div.field
              [:div.control
               [:a {:href "/#/register"} "New user?"]]]
             ]))]]]))
