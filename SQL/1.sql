
CREATE TABLE type_of_musician ( id SERIAL PRIMARY KEY,
                                                  name VARCHAR(100) NOT NULL);


CREATE TABLE genre ( id SERIAL PRIMARY KEY,
                                       name VARCHAR(100) NOT NULL);


CREATE TABLE brand ( id SERIAL PRIMARY KEY,
                                       name VARCHAR(100) NOT NULL);


CREATE TABLE guitar_form ( id SERIAL PRIMARY KEY,
                                             name VARCHAR(100) NOT NULL);


CREATE TABLE type_of_product ( id SERIAL PRIMARY KEY,
                                                 name VARCHAR(100) NOT NULL);


CREATE TABLE type_of_user ( id SERIAL PRIMARY KEY,
                                              name VARCHAR(100) NOT NULL);


CREATE TABLE musician
    ( id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                                          subscribers INTEGER NOT NULL,
                                                              genre_id INTEGER REFERENCES genre(id) ON DELETE
     SET NULL,
         type_of_musician_id INTEGER REFERENCES type_of_musician(id) ON DELETE
     SET NULL);


CREATE TABLE "user"
    ( id SERIAL PRIMARY KEY,
                        is_admin BOOLEAN DEFAULT FALSE,
                                                 login VARCHAR(100) UNIQUE NOT NULL,
                                                                           password TEXT NOT NULL,
                                                                                         password_salt TEXT NOT NULL,
                                                                                                            type_of_user_id INTEGER REFERENCES type_of_user(id) ON DELETE
     SET NULL,
         subscriptions INTEGER);


CREATE TABLE product
    ( id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                                          description TEXT, price NUMERIC(10, 2) NOT NULL,
                                                                                 rate NUMERIC(2, 1),
                                                                                      brand_id INTEGER REFERENCES brand(id) ON DELETE
     SET NULL,
         guitar_form_id INTEGER REFERENCES guitar_form(id) ON DELETE
     SET NULL,
         type_of_product_id INTEGER REFERENCES type_of_product(id) ON DELETE
     SET NULL,
         lads INTEGER, strings INTEGER, tip_material VARCHAR(100),
                                                     body_material VARCHAR(100),
                                                                   pickup_configuration VARCHAR(100),
                                                                                        type_combo_amplifier VARCHAR(100));


CREATE TABLE feedback
    ( id SERIAL PRIMARY KEY,
                        user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
                                                                        product_id INTEGER REFERENCES "product"(id) ON DELETE CASCADE,
                                                                                                                              article_id INTEGER REFERENCES "articles"(id) ON DELETE CASCADE, text TEXT, stars INTEGER CHECK (stars >= 1
                                                                                                                                                                                                                              AND stars <= 5));


CREATE TABLE articles
    ( id SERIAL PRIMARY KEY,
                        header VARCHAR(200) NOT NULL, text TEXT NOT NULL,
                                                                author INTEGER REFERENCES "user"(id) ON DELETE
     SET NULL, date DATE NOT NULL,
                         tags VARCHAR(200),
                              accepted BOOLEAN DEFAULT FALSE);


CREATE TABLE user_musician_subscription
    ( id SERIAL PRIMARY KEY,
                        user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
                                                                        musician_id INTEGER REFERENCES musician(id) ON DELETE CASCADE,
                                                                                                                              PRIMARY KEY (user_id,
                                                                                                                                           musician_id));


CREATE TABLE musician_genre
    ( id SERIAL PRIMARY KEY,
                        musician_id INTEGER REFERENCES musician(id) ON DELETE CASCADE,
                                                                              genre_id INTEGER REFERENCES genre(id) ON DELETE CASCADE,
                                                                                                                              PRIMARY KEY (musician_id,
                                                                                                                                           genre_id));


CREATE TABLE product_genre
    ( id SERIAL PRIMARY KEY,
                        product_id INTEGER REFERENCES product(id) ON DELETE CASCADE,
                                                                            genre_id INTEGER REFERENCES genre(id) ON DELETE CASCADE,
                                                                                                                            PRIMARY KEY (product_id,
                                                                                                                                         genre_id));


CREATE TABLE type_of_user_user
    ( id SERIAL PRIMARY KEY,
                        type_of_user_id INTEGER REFERENCES type_of_user(id) ON DELETE CASCADE,
                                                                                      user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
                                                                                                                                      PRIMARY KEY (type_of_user_id,
                                                                                                                                                   user_id));


CREATE TABLE type_of_musician_user
    ( id SERIAL PRIMARY KEY,
                        type_of_musician_id INTEGER REFERENCES type_of_musician(id) ON DELETE CASCADE,
                                                                                              user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
                                                                                                                                              PRIMARY KEY (type_of_musician_id,
                                                                                                                                                           user_id));


CREATE TABLE genre_user
    ( id SERIAL PRIMARY KEY,
                        genre_id INTEGER REFERENCES genre(id) ON DELETE CASCADE,
                                                                        user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
                                                                                                                        PRIMARY KEY (genre_id,
                                                                                                                                     user_id));


CREATE TABLE musician_product
    ( id SERIAL PRIMARY KEY,
                        musician_id INTEGER REFERENCES musician(id) ON DELETE CASCADE,
                                                                              product_id INTEGER REFERENCES product(id) ON DELETE CASCADE,
                                                                                                                                  PRIMARY KEY (musician_id,
                                                                                                                                               product_id));


CREATE TABLE product_user
    ( id SERIAL PRIMARY KEY,
                        product_id INTEGER REFERENCES product(id) ON DELETE CASCADE,
                                                                            user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
                                                                                                                            PRIMARY KEY (product_id,
                                                                                                                                         user_id));


CREATE TABLE product_articles
    ( id SERIAL PRIMARY KEY,
                        product_id INTEGER REFERENCES product(id) ON DELETE CASCADE,
                                                                            article_id INTEGER REFERENCES articles(id) ON DELETE CASCADE,
                                                                                                                                 PRIMARY KEY (product_id,
                                                                                                                                              article_id));

