(ns booksclubwithauth.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [booksclubwithauth.ajax :as ajax]
    [booksclubwithauth.subs]
    [booksclubwithauth.effects]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string]
    [booksclubwithauth.pages.book :as book]
    [booksclubwithauth.pages.login :as login]
    [booksclubwithauth.pages.register :as register]
    [booksclubwithauth.pages.user :as user]
    [booksclubwithauth.events.login-registration]
    [booksclubwithauth.events.books])
  (:import goog.History))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])


(defn navbar [] 
  (r/with-let [expanded? (r/atom false)
               username (rf/subscribe [:user-name])
               profile-image (rf/subscribe [:user-profile-image])]
              [:nav.navbar.is-transparent>div.container
               [:div.navbar-brand
                [:a.navbar-item {:href "/#" :style {:font-weight :bold}} "BooksClub"]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click #(swap! expanded? not)
                  :class (when @expanded? :is-active)}
                 [:span][:span][:span]]]
               [:div#nav-menu.navbar-menu
                {:class (when @expanded? :is-active)}
                [:div.navbar-start
                 [nav-link "#/" "Home" :home]
                 [nav-link "#/about" "About" :about]
                 (if (some? @username)
                   [:<>
                    [nav-link "#/books" "Books" :my-books]
                    [nav-link "#/users" "Users" :search-users]])]
                [:div.navbar-end
                 (if (some? @username)
                   [:<>
                    [:img.image.is-32x32.is-rounded {:src @profile-image}]
                    [:p @username]
                    [:a {:on-click (fn [e]
                                     (.preventDefault e)
                                     (rf/dispatch [:logout]))} "Logout"]]
                   [:<>
                    [nav-link "#/login" "Login" :login]
                    [nav-link "#/register" "Register" :register]])]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn home-page []
  [:section.section>div.container>div.content
   (when-let [docs @(rf/subscribe [:docs])]
     [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [navbar]
     [page]]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]
     ["/login" {:name :login
                   :view login/login}]
     ["/register" {:name :register
                   :view register/register}]
     ["/book/add" {:name :add-book
                   :view book/add-book
                   :controllers [{:start (fn [_]
                                           (rf/dispatch [:check-authentication]))}]}]

     ["/book/:id/edit" {:name :update-book
                        :view book/update-book
                        :controllers [{:parameters {:path [:id]}
                                       :start (fn [{:keys [path]}]
                                                (rf/dispatch [:check-authentication])
                                                (let [{id :id} path]
                                                  (rf/dispatch [:fetch-book-details id])))}]}]

     ["/books" {:name :my-books
                :view book/my-books
                :controllers [{:start (fn [_]
                                        (rf/dispatch [:check-authentication])
                                        (rf/dispatch [:my-read-books]))}]}]

     ["/users" {:name :search-users
                :view user/search-users
                :controllers [{:start (fn [_]
                                        (rf/dispatch [:check-authentication]))}]}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------


;; Initialize app
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(rf/dispatch-sync [:initialise-db])

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
