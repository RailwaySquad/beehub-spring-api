FROM openjdk:21-jdk-alpine

# Thiết lập JAVA_OPTS
ENV JAVA_OPTS=""

# Sao chép file Maven Wrapper trước và cấp quyền thực thi
COPY . /app
WORKDIR /app
RUN chmod +x ./mvnw

# Build dự án mà không chạy các bài test
RUN ./mvnw clean install -DskipTests

# Chạy ứng dụng
CMD ["java", "-jar", "target/beehub-0.0.1-SNAPSHOT.jar"