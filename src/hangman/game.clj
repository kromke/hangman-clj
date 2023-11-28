(ns hangman.game
  (:require [clojure.java.shell :as sh]))

(declare interaction! clear-terminal! output! read-char! update-by-input)

(def gallows (read-string (slurp "resources/gallows.edn")))

(defn game!
  [word]
  (loop [state {:word word,
                :gallows-level 0,
                :guessed (repeat (count word) \-),
                :letters #{}}]
    (let [state* (interaction! state)
          win? (fn [{:keys [word guessed]}] (= word guessed))
          win #(do (clear-terminal!) (println "Вы победили!"))
          lose? (fn [{:keys [gallows-level]}] (= gallows-level 7))
          lose #(do (clear-terminal!)
                    (println "Вас повесили!")
                    (println (get gallows 7)))]
      (cond (win? state*) (win)
            (lose? state*) (lose)
            :else (recur state*)))))

(defn- interaction!
  "Взаимодействие с игроком, вывод информации, получение буквы,
  обновление состояния по результату проверки."
  [{:keys [letters], :as state}]
  (clear-terminal!)
  (output! state)
  (update-by-input state (read-char! letters)))

(defn- clear-terminal! [] (sh/sh "clear"))

(defn- output!
  [{:keys [gallows-level guessed]}]
  (println (str (get gallows gallows-level)
                "\n"
                "Угадайте слово: "
                (apply str guessed)))
  (print "Наберите букву и нажмите enter: "))

(defn- read-char!
  [letters]
  (let [valid? (fn [in] (or (re-matches #"[а-яА-я]" in) (letters in)))]
    (loop [in (read-line)]
      (print in)
      (if (valid? in)
        in
        (do (print (apply str
                          (conj (repeat 36 "\r")
                                "Введите новую букву и нажмите enter: ")))
            (recur (read-line)))))))

(defn- update-by-input
  [{:keys [word guessed], :as state} input]
  (let [*state (update state :letters conj input)
        indxs (set (map-indexed #(when (= input %2) %1) word))]
    (if (seq indxs)
      (assoc *state
             :guessed (apply str (map-indexed #(if (indxs %1) input %2) guessed)))
      (update *state :gallows-level inc))))

(comment
  (def test-word "собака")
  (def test-state {:word test-word,
                   :gallows-level 0,
                   :guessed (repeat (count test-word) \-),
                   :letters #{}})

  (read-char! #{})
  (game! "собака"))
