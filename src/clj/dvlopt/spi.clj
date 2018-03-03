(ns dvlopt.spi

  "Talk to SPI devices from Linux."

  {:author "Adam Helinski"}

  (:import (java.io IOException
                    RandomAccessFile)
           (sun.misc SharedSecrets
                     JavaIOFileDescriptorAccess)
           (com.sun.jna NativeLong
                        Pointer
                        Memory)
           clojure.lang.Sequential
           (dvlopt.spi Spi
                       Spi$Requests
                       SpiIOCTransfer)))




;;;;;;;;;; File descriptors


(def ^:private ^JavaIOFileDescriptorAccess -fd-access

  "Object for accessing the linux file descriptor with a java file descriptor."

  (SharedSecrets/getJavaIOFileDescriptorAccess))




(defn- -fd

  "Get the linux file descriptor of a RandomAccessFile."

  [^RandomAccessFile file]

  (.get -fd-access
        (.getFD file)))




;;;;;;;;;; SPI - Private - Configuration


(defn- -rd-bits-per-word

  "Sets bits per word for reading."

  [fd n]

  (when (and n
             (= (Spi/ioctl fd
                           Spi$Requests/SPI_IOC_RD_BITS_PER_WORD
                           n)
                -1))
    (throw (IOException. (format "Unable to set the number of bits per word for reads to '%d'."
                                 n)))))




(defn- -rd-lsb-first

  "Sets endianess for reading."

  [fd lsb-first?]

  (when (= (Spi/ioctl fd
                      Spi$Requests/SPI_IOC_RD_LSB_FIRST
                      (if lsb-first?
                        Spi$Requests/SPI_LSB_FIRST
                        0))
           -1)
    (throw (IOException. (format "Unable to set reading in LSB first mode to '%b'."
                                 lsb-first?)))))




(defn- -rd-max-speed

  "Sets the maximum speed for reading."

  [fd hz]

  (when (and hz
             (= (Spi/ioctl fd
                           Spi$Requests/SPI_IOC_RD_MAX_SPEED_HZ
                           hz)
                -1))
    (throw (IOException. (format "Unable to set the maximum reading speed to '%d'."
                                 hz)))))




(defn- -rd-mode

  "Sets read mode."

  [fd mode]

  (when (and mode
             (= (Spi/ioctl fd
                 Spi$Requests/SPI_IOC_RD_MODE
                 mode)
                -1))
    (throw (IOException. (format "Unable to set reading mode to '%d'"
                                 mode)))))




(defn- -wr-bits-per-word

  "Sets bits per word for writing."

  [fd n]

  (when (and n
             (= (Spi/ioctl fd
                           Spi$Requests/SPI_IOC_WR_BITS_PER_WORD
                           n)
                -1))
    (throw (IOException. (format "Unable to set the number of bits per word for writes to '%d'."
                                 n)))))




(defn- -wr-lsb-first

  "Sets endianess for writing."

  [fd lsb-first?]

  (when (= (Spi/ioctl fd
                      Spi$Requests/SPI_IOC_WR_LSB_FIRST
                      (if lsb-first?
                        Spi$Requests/SPI_LSB_FIRST
                        0))
           -1)
    (throw (IOException. (format "Unable to set writing in LSB first mode to '%b'."
                                 lsb-first?)))))




(defn- -wr-max-speed

  "Sets the maximum speed for writing."

  [fd hz]

  (when (and hz
             (= (Spi/ioctl fd
                           Spi$Requests/SPI_IOC_WR_MAX_SPEED_HZ
                           hz)
                -1))
    (throw (IOException. (format "Unable to set the maximum writing speed to '%d'."
                                 hz)))))




(defn- -wr-mode

  "Sets write mode."

  [fd mode]

  (when (and mode
             (= (Spi/ioctl fd
                           Spi$Requests/SPI_IOC_WR_MODE
                           mode)
                -1))
    (throw (IOException. (format "Unable to set writing mode to '%d'"
                                 mode)))))




;;;;;;;;;; SPI - Private


(defn -ioc-transfer

  "Executes a full-duplex transfer."

  ^SpiIOCTransfer

  [{:as   opts
    :keys [^bytes tx-buff
           ^bytes rx-buff
           delay-micros
           hz
           bits-per-word
           cs-change]}]

  (let [ioc-transfer (SpiIOCTransfer.)
        cnt-tx-buff  (count tx-buff)
        cnt-rx-buff  (count rx-buff)
        ^Memory
        mem-tx-buff  (when (pos? cnt-tx-buff)
                       (Memory. cnt-tx-buff))
        ^Memory
        mem-rx-buff  (when (pos? cnt-rx-buff)
                       (Memory. cnt-rx-buff))
        len          (if tx-buff
                       (if rx-buff
                         (min cnt-tx-buff
                              cnt-rx-buff)
                         cnt-tx-buff)
                       (if rx-buff
                         cnt-rx-buff
                         nil))]
    (when mem-tx-buff
      (.write mem-tx-buff
              0
              tx-buff
              0
              cnt-tx-buff)
      (set! (.txBuff ioc-transfer)
            (NativeLong. (Pointer/nativeValue mem-tx-buff)
                                              true)))
    (when mem-rx-buff
      (.write mem-rx-buff
              0
              rx-buff
              0
              cnt-rx-buff)
      (set! (.rxBuff ioc-transfer)
            (NativeLong. (Pointer/nativeValue mem-rx-buff)
                                              true)))
    (some->> len
             (set! (.-len ioc-transfer)))
    (some->> delay-micros
             (set! (.-delayMicros ioc-transfer)))
    (some->> bits-per-word
             (set! (.-bitsPerWord ioc-transfer)))
    (some->> hz
             (set! (.-speedHz ioc-transfer)))
    (when cs-change
      (set! (.-csChange ioc-transfer)
            (if cs-change
              1
              0)))
    ioc-transfer))




;;;;;;;;;; SPI - API


(defprotocol SPI

  ""

  (configure [this]
             [this opts]

    "Configures this channel.")


  (read-byte [this]

    "Reads a single byte.")


  (read-bytes [this ba]
              [this ba offset n]

    "Reads several bytes.")


  (write-byte [this b]

    "Writes a single byte.")


  (write-bytes [this bs]
               [this ba offset n]
               
    "Writes several bytes.")


  (transfer [this opts]
            
    "Executes a full-duplex transfer.")


  (close [this]

    "Closes this channel."))




(deftype SPIChannel [;^String           path
                     ^RandomAccessFile file
                                       fd
                     ;^boolean          closed?
                     ^SpiIOCTransfer   ioc-transfer]

  SPI

    (configure [this {:as   opts
                      :keys [rd-mode
                             wr-mode
                             rd-word
                             wr-word
                             rd-lsb-first?
                             wr-lsb-first?
                             rd-max-speed
                             wr-max-speed]}]

      (-rd-mode fd
                rd-mode)
      (-wr-mode fd
                wr-mode)
      (-rd-bits-per-word fd
                         rd-word)
      (-wr-bits-per-word fd
                         wr-word)
      (-rd-lsb-first fd
                     rd-lsb-first?)
      (-wr-lsb-first fd
                     wr-lsb-first?)
      (-rd-max-speed fd
                     rd-max-speed)
      (-wr-max-speed fd
                     wr-max-speed)
      this)


    (read-byte [_]
      (.read file))


    (read-bytes [_ ba]
      (.read file
             ba))


    (read-bytes [_ ba offset n]
      (.read file
             ba
             offset
             n))


    (write-byte [_ b]
      (.write file
              ^long b))


    (write-bytes [_ ba]
      (.write file
              ^bytes ba))


    (write-bytes [_ ba offset n]
      (.write file
              ba
              offset
              n))


    (transfer [_ transactions]
      (let [ioc-transfers (map -ioc-transfer
                               transactions)]
        (when (and (seq ioc-transfers)
                   (= (.ioctl Spi/proxy
                              fd
                              (Spi$Requests/iocMessage (count transactions))
                              (into-array SpiIOCTransfer
                                          ioc-transfers))
                      -1)
               (throw (IOException. "Transfer failed."))))
        (map (fn ??? [^SpiIOCTransfer ioc-transfer]
               ;; TODO fn name + write data to user given arrays
               (let [^Memory
                     mem-tx-buff (.-txBuff ioc-transfer)
                     ^Memory
                     mem-rx-buff (.-rxBuff ioc-transfer)]
                 {:tx-buff (.getByteArray mem-tx-buff
                                          0
                                          (.size mem-tx-buff))
                  :rx-buff (.getByteArray mem-rx-buff
                                          0
                                          (.size mem-rx-buff))}))
             ioc-transfers)))


    (close [_]
      (.close file)))




(defn open

  "Opens an SPI channel."

  ([]

   (open nil))


  ([{:as   opts
     :keys [^String path]}]

   (let [file (RandomAccessFile. path
                                 "rw")
         fd   (-fd file)]
     (try
       (SPIChannel. file
                    fd
                    (SpiIOCTransfer.))
       ;; TODO should configure here ?
       (catch Throwable e
         (.close file)
         (throw e))))))
