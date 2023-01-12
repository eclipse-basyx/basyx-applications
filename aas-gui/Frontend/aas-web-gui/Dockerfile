FROM node

RUN git clone https://github.com/eclipse-basyx/basyx-applications.git
RUN npm install -g @vue/cli
RUN ls basyx-applications/aas-gui/Frontend
RUN yarn --cwd basyx-applications/aas-gui/Frontend/aas-web-gui install

EXPOSE 3000/tcp

CMD yarn --cwd basyx-applications/aas-gui/Frontend/aas-web-gui dev