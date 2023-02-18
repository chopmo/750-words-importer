(ns journal-importer.main
  (:require [java-time :as jt]
            [clojure.string :as str]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]))


(def entry-marker "------ ENTRY ------")

(def broken-encoding-marker "Ã¦")

(defn parse-entry
  [lines]
  (let [date-str    (first (filter #(str/starts-with? % "Date:") lines))
        date        (jt/local-date (first (re-seq #"\d+.*" date-str)))
        entry-lines (drop 4 lines)
        entry-text  (str/join "\n" entry-lines)]
    {:date    date
     :weekday (str/capitalize (jt/day-of-week date))
     :entry   (str/trim entry-text)}))

(defn extract-entries
  [contents]
  (let [lines   (str/split-lines contents)
        groups  (partition-by #(= entry-marker %) lines)
        entries (filter #(not= entry-marker (first %)) groups)]
    (map parse-entry entries)))

(defn input-files
  []
  (filter #(.isFile %) (file-seq (io/file "input"))))

(defn broken-encoding?
  [f]
  (boolean (str/index-of (slurp f) broken-encoding-marker)))

(defn read-contents
  [f]
  (if (broken-encoding? f)
    (:out (sh "iconv" "-f" "UTF-8" "-t" "ISO-8859-1" (str f)))
    (slurp f)))

(defn convert-new-entry
  [s]
  (-> s
      (str/replace "===== ENTRY =====" entry-marker)
      (str/replace "=== DATE:" "Date:")
      (str/replace "=== BODY ===" "")
      (str/replace " ===" "")))

(convert-new-entry (slurp "input/750words_export_2023_01"))
(defn write-entry
  [e]
  (let [filename            (str "output/"
                                 (jt/format (jt/formatter :basic-iso-date)
                                            (:date e))
                                 ".org")
        header-date         (jt/format (jt/formatter "dd-MM-YYYY") (:date e))
        header              (str "* " (:weekday e) ", " header-date)
        content-with-header (str header "\n\n" (:entry e) "\n")]
    (spit filename content-with-header)
    filename))

(defn encrypt-file
  [filename]
  (sh "gpg" "-e" "-r" "jacob@tjoernholm.dk" filename)
  (io/delete-file filename))

(defn delete-all-output-files
  []
  (doseq [f (filter #(.isFile %) (file-seq (io/file "output")))]
    (io/delete-file f)))

(defn convert-all
  []
  (delete-all-output-files)
  (doseq [f (input-files)]
    (let [contents (convert-new-entry (read-contents f))
          entries  (extract-entries contents)]
      (doseq [e entries]
        (print "Writing entry for"
               (str (:date e))
               "with content size"
               (count (:entry e))
               "\n")
        (encrypt-file (write-entry e))
        ;(write-entry e)
      ))))

(defn main
  [opts]
  (convert-all))
