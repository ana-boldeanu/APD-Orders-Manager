FROM jokeswar/base-ctl

RUN apt-get update -yqq && apt-get install -yqq bc
RUN apt-get update -yqq && apt-get install -yqq wget
RUN wget https://download.oracle.com/java/19/latest/jdk-19_linux-x64_bin.deb
RUN apt-get -qqy install ./jdk-19_linux-x64_bin.deb
RUN update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-19/bin/java 1919
RUN update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk-19/bin/javac 1919

COPY ./checker ${CHECKER_DATA_DIRECTORY}
