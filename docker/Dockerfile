FROM ubuntu:18.04

RUN apt-get update

RUN apt-get install -y build-essential

RUN apt-get install -y openjdk-11-jre-headless

#Required by perl module installer
RUN apt-get install -y libnet-ssleay-perl
RUN apt-get install -y libcrypt-ssleay-perl

#Command-line JSON processor
RUN apt-get install -y jq

RUN cpan App::cpanminus

RUN cpanm --notest REST::Client
RUN cpanm --notest JSON::MaybeXS
RUN cpanm --notest XML::Fast

WORKDIR /opt/subs-checklist-service

COPY subs-checklist-service.jar /opt/subs-checklist-service

ADD convert_to_usi.pl /opt/subs-checklist-service

ENTRYPOINT java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar subs-checklist-service.jar
