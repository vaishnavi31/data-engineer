FROM node:18 as build

WORKDIR /app

COPY package*.json ./

RUN npm install 

COPY . .

RUN npm run build

FROM nginx:1.25-alpine as production-stage

RUN rm -rf /usr/share/nginx/html/*

COPY --from=build /app/dist/splash /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
