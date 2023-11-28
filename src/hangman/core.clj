(ns hangman.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [hangman.game :as game])
  (:gen-class))

(def words (read-string (slurp "resources/words.edn")))
;; получить слово на сайте генераторе
(defn- get-generator-word!
  "Получает по урлу и парсит слово"
  [generator-url]
  (-> (client/get generator-url)
      :body
      (json/read-json)
      :word
      :word))

(defn -main [] (game/game! (get-generator-word! (:url words))))
