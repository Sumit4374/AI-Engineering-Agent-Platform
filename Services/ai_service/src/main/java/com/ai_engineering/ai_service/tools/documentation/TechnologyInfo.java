package com.ai_engineering.ai_service.tools.documentation;

public class TechnologyInfo {

    public record Urls(
        String languageUrl,
        String frameworkUrl,
        String databaseUrl
    ){}

    public record Technology(
        Language language,
        Framework framework,
        Database database
    ){}

    public enum Language {
        JAVA,
        PYTHON,
        JAVASCRIPT,
        CSHARP,
        RUBY,
        PHP,
        SWIFT,
        KOTLIN,
        GO,
        RUST;

        public  Language language(String language) {
            return switch (language.toLowerCase()) {
                case "java" -> JAVA;
                case "python" -> PYTHON;
                case "javascript" -> JAVASCRIPT;
                case "c#" -> CSHARP;
                case "ruby" -> RUBY;
                case "php" -> PHP;
                case "swift" -> SWIFT;
                case "kotlin" -> KOTLIN;
                case "go" -> GO;
                case "rust" -> RUST;
                default -> throw new IllegalArgumentException("Language not Supported: " + language);
            };
        }

        public String getLink() {
            return switch (this) {
                case JAVA -> "https://www.java.com/";
                case PYTHON -> "https://www.python.org/";
                case JAVASCRIPT -> "https://developer.mozilla.org/en-US/docs/Web/JavaScript";
                case CSHARP -> "https://docs.microsoft.com/en-us/dotnet/csharp/";
                case RUBY -> "https://www.ruby-lang.org/en/";
                case PHP -> "https://www.php.net/";
                case SWIFT -> "https://swift.org/";
                case KOTLIN -> "https://kotlinlang.org/";
                case GO -> "https://golang.org/";
                case RUST -> "https://www.rust-lang.org/";
            };
        }
    }

    public enum Framework {
        SPRING,
        DJANGO,
        EXPRESS,
        ASP_NET,
        RAILS,
        LARAVEL,
        FLUTTER,
        REACT_NATIVE,
        ANGULAR,
        VUE;

        public Framework framework(String framework) {
            return switch (framework.toLowerCase()) {
                case "spring" -> SPRING;
                case "django" -> DJANGO;
                case "express" -> EXPRESS;
                case "asp.net" -> ASP_NET;
                case "rails" -> RAILS;
                case "laravel" -> LARAVEL;
                case "flutter" -> FLUTTER;
                case "react native" -> REACT_NATIVE;
                case "angular" -> ANGULAR;
                case "vue" -> VUE;
                default -> throw new IllegalArgumentException("Framework not Supported: " + framework);
            };
        }


        public String getLink() {
            return switch (this) {
                case SPRING -> "https://spring.io/projects/spring-framework";
                case DJANGO -> "https://www.djangoproject.com/";
                case EXPRESS -> "https://expressjs.com/";
                case ASP_NET -> "https://dotnet.microsoft.com/apps/aspnet";
                case RAILS -> "https://rubyonrails.org/";
                case LARAVEL -> "https://laravel.com/";
                case FLUTTER -> "https://flutter.dev/";
                case REACT_NATIVE -> "https://reactnative.dev/";
                case ANGULAR -> "https://angular.io/";
                case VUE -> "https://vuejs.org/";
            };
        }
    }

    public enum Database {
        MYSQL,
        POSTGRESQL,
        MONGODB,
        SQLITE,
        ORACLE,
        SQLSERVER,
        REDIS,
        CASSANDRA,
        ELASTICSEARCH;

        public Database database(String database) {
            return switch (database.toLowerCase()) {
                case "mysql" -> MYSQL;
                case "postgresql" -> POSTGRESQL;
                case "mongodb" -> MONGODB;
                case "sqlite" -> SQLITE;
                case "oracle" -> ORACLE;
                case "sqlserver" -> SQLSERVER;
                case "redis" -> REDIS;
                case "cassandra" -> CASSANDRA;
                case "elasticsearch" -> ELASTICSEARCH;
                default -> throw new IllegalArgumentException("Database not Supported: " + database);
            };
        }

        public String getLink() {
            return switch (this) {
                case MYSQL -> "https://www.mysql.com/";
                case POSTGRESQL -> "https://www.postgresql.org/";
                case MONGODB -> "https://www.mongodb.com/";
                case SQLITE -> "https://www.sqlite.org/";
                case ORACLE -> "https://www.oracle.com/database/";
                case SQLSERVER -> "https://www.microsoft.com/en-us/sql-server/";
                case REDIS -> "https://redis.io/";
                case CASSANDRA -> "https://cassandra.apache.org/";
                case ELASTICSEARCH -> "https://www.elastic.co/elasticsearch/";
            };
        }
    }
}
