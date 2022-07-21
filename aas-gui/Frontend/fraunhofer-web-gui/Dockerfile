FROM node

RUN git clone https://github.com/eclipse-basyx/basyx-applications.git
RUN npm install -g @vue/cli
RUN npm install --prefix /basyx-applications/aas-gui/Frontend/fraunhofer-web-gui

EXPOSE 8080/tcp

CMD npm run serve --prefix /basyx-applications/aas-gui/Frontend/fraunhofer-web-gui