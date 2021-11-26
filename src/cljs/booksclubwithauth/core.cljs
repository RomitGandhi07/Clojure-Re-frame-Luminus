(ns booksclubwithauth.core
  (:require
    [day8.re-frame.http-fx]
    [cljs.core.match :refer-macros [match]]
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
    [booksclubwithauth.pages.chart :as chart]
    [booksclubwithauth.pages.followers-list :as followers]
    [booksclubwithauth.events.login-registration]
    [booksclubwithauth.events.books]
    [booksclubwithauth.events.users]
    ["react-toastify" :refer [ToastContainer]])
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
                    [nav-link "#/users" "Users" :search-users]
                    [nav-link "#/followers" "Followers" :followers]
                    [nav-link "#/chart" "Demo Chart" :chart]])]
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
    [:<>
     [:> ToastContainer
      {:position "top-center"
       :autoClose 3000
       :closeOnClick true}]
     [:div
     [navbar]
     [page]]]
    [:div
     [:p "404 Page"]]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match])
  (rf/dispatch [:clear-error]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :public?     true
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page
                :public? true}]
     ["/login" {:name :login
                :view login/login
                :public? true}]
     ["/register" {:name :register
                   :view register/register
                   :public? true}]
     ["/book/add" {:name :add-book
                   :view book/add-book}]

     ["/book/:id/edit" {:name :update-book
                        :view book/update-book
                        :controllers [{:parameters {:path [:id]}
                                       :start (fn [{:keys [path]}]
                                                (let [{id :id} path]
                                                  (rf/dispatch [:fetch-book-details id])))}]}]

     ["/books" {:name :my-books
                :view book/my-books
                :controllers [{:start (fn [_]
                                        (rf/dispatch [:my-read-books]))}]}]

     ["/users" {:name :search-users
                :view user/search-users}]

     ["/followers" {:name :followers
                :view followers/followers-list}]

     ["/chart" {:name :chart
                :view chart/chartjs-component
                :public? true}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------

(defn router-component
  [{:keys [router]}]
  (let [current-route (rf/subscribe [:common/route])
        user?         (not (nil? @(rf/subscribe [:user])))
        route-data    (merge
                        (select-keys @current-route [:parameters])
                        (select-keys (:data @current-route) [:public? :view :name])
                        {:user user?})
        public?       (:public? route-data)]
    (when @current-route
      (match [public? user?]
             [nil false] (rf/dispatch [:navigate-login!])
             :else (page)))))

;; Initialize app
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [router-component {:router router}] (.getElementById js/document "app")))

(rf/dispatch-sync [:initialise-db])

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
