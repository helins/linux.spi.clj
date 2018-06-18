(ns user

  "For daydreaming in the repl."

  (:require [clojure.spec.alpha              :as s]
            [clojure.spec.gen.alpha          :as gen]
            [clojure.spec.test.alpha         :as st]
            [clojure.test.check.clojure-test :as tt]
            [clojure.test.check.generators   :as tgen]
            [clojure.test.check.properties   :as tprop]
            [clojure.test                    :as t]
            [criterium.core                  :as ct]
            [dvlopt.linux.spi                :as spi])
  (:import (com.sun.jna Memory
                        Native
                        NativeLong
                        Pointer)
           (dvlopt.spi Spi
                       Spi$Requests
                       SpiIOCTransfer)
           java.io.RandomAccessFile))




;;;;;;;;;;


(comment
  

  )
