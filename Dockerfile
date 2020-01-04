#Dockerfile for encoder
FROM fabric8/java-centos-openjdk8-jdk:1.5
ENV JAVA_APP_DIR=/deployments
LABEL org.label-schema.description="Demo project for Spring Boot" org.label-schema.version=0.0.1-SNAPSHOT org.label-schema.schema-version=1.0 org.label-schema.build-date=2020-01-02T14:09:27.650440 org.label-schema.name=video-encoder org.label-schema.vcs-ref=f8a3e7b1f13799a01c43c33d430e1df67002329e org.label-schema.url=https://projects.spring.io/spring-boot/#/spring-boot-starter-parent/video-encoder org.label-schema.vcs-url=https://github.com/ljungberg84/video-encoder.git
EXPOSE 8083 8778 9779
USER root
#check if this works for only copying jar
COPY ./target/*.jar /deployments/
COPY set_paths.sh /etc/profile.d/

RUN yum -y update

#ffmpeg
RUN yum -y install git epel-release
RUN rpm -v &#45;&#45;import http://li.nux.ro/download/nux/RPM-GPG-KEY-nux.ro
RUN rpm -Uvh http://li.nux.ro/download/nux/dextop/el7/x86_64/nux-dextop-release-0-5.el7.nux.noarch.rpm
RUN yum -y install ffmpeg ffmpeg-devel

#shaka packager build dependencies
RUN yum -y install python git curl gcc-c++ findutils bzip2 ncurses-compat-libs

#shaka packager
RUN git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git
ENV PATH="$PATH:/depot_tools"
RUN mkdir shaka_packager
RUN cd shaka_packager && gclient config https://www.github.com/google/shaka-packager.git --name=src --unmanaged
RUN cd shaka_packager && gclient sync
RUN cd shaka_packager/src && ninja -C out/Release
ENV PATH="$PATH:/shaka_packager/src/out/Release"
