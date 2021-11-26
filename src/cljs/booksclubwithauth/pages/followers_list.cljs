(ns booksclubwithauth.pages.followers-list
  (:require
    ["ag-grid-react" :refer [AgGridReact]]
    [reagent.core :as r]))

(defn demo
  [e]
  (js/console.log e)
  (r/as-element
    [:b (-> e .-value)]))

(defn followers-list
  []
  [:div.ag-theme-balham {:style {:height 500
                                 :width 700}}
   [:> AgGridReact
    {:columnDefs [{:headerName "Make" :field :make :cellRendererFramework demo}
                 {:headerName "Model" :field :model}
                 {:headerName "Price"
                  :field :price
                  :cellRenderer (fn [e]
                                  (str (-> e .-value) "$"))}]
     :rowData [{:make "Toyota" :model "Celica" :price 35000},
               {:make "Ford" :model "Mondeo" :price 32000},
               {:make "Porsche" :model "Boxter" :price 72000}]}]])