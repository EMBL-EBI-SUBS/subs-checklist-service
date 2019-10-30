FROM picoded/ubuntu-openjdk-8-jdk

RUN apt-get update

#Required by perl module installer
RUN apt-get install -y libssl-dev

#Command-line JSON processor
RUN apt-get install -y jq

RUN cpan App::cpanminus

RUN cpanm --notest REST::Client
RUN cpanm --notest JSON::MaybeXS
RUN cpanm --notest XML::Fast

ADD subs-checklist-service.tar /opt

RUN mv /opt/subs-checklist-service-* /opt/subs-checklist-service

ADD convert_to_usi.pl /opt/subs-checklist-service

WORKDIR /opt/subs-checklist-service/bin

ENTRYPOINT ./subs-checklist-service
