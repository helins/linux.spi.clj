(defproject dvlopt/linux.spi
            "0.0.0-alpha0"

  :description       "Talk to SPI devices from Linux"
  :url               "https://github.com/dvlopt/linux.spi"
  :license           {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths      ["src/clj"]
  :java-source-paths ["src/java"]
  :dependencies      [[net.java.dev.jna/jna "4.5.0"]]
  :profiles          {:dev {:source-paths ["dev"]
                            :main         user
                            :dependencies [[criterium              "0.4.4"]
                                           [org.clojure/clojure    "1.9.0"]
                                           [org.clojure/test.check "0.10.0-alpha2"]]
                            :plugins      [[lein-codox      "0.10.3"]
                                           [venantius/ultra "0.5.2"]]
                            :codox        {:output-path  "doc/auto"
                                           :source-paths ["src"]}
                            :repl-options {:timeout 180000}
                            :global-vars  {*warn-on-reflection* true}}})
