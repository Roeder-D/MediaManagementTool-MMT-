DROP DATABASE IF EXISTS mediamanagement;
CREATE DATABASE IF NOT EXISTS mediamanagement;

USE mediamanagement;


CREATE TABLE tag(
                    tag_id				INTEGER			PRIMARY KEY AUTO_INCREMENT,
                    tag					VARCHAR(50)		UNIQUE
);

CREATE TABLE series(
                       series_id			INTEGER			PRIMARY KEY AUTO_INCREMENT,
                       series_name			VARCHAR(350),
                       number_of_titles	INTEGER,
                       start_year			YEAR
);

CREATE TABLE mediatype(
                          mediatype_id		INTEGER			PRIMARY KEY AUTO_INCREMENT,
                          type_name			VARCHAR(50)		UNIQUE
);

CREATE TABLE publisher(
                          publisher_id		INTEGER			PRIMARY KEY AUTO_INCREMENT,
                          publisher_name		VARCHAR(50)		UNIQUE
);

CREATE TABLE artist(
                       artist_id			INTEGER			PRIMARY KEY AUTO_INCREMENT,
                       first_name			VARCHAR(50),
                       last_name			VARCHAR(50),
                       nationality			VARCHAR(50)
);

CREATE TABLE artist_role(
                            artist_role_id		INTEGER			PRIMARY KEY AUTO_INCREMENT,
                            role				VARCHAR(50)
);

CREATE TABLE franchise(
                          franchise_id		INTEGER			PRIMARY KEY AUTO_INCREMENT,
                          franchise_name		VARCHAR(500)
);

CREATE TABLE genre(
                      genre_id			INTEGER			PRIMARY KEY AUTO_INCREMENT,
                      genre_name			VARCHAR(50)		UNIQUE
);

CREATE TABLE lendee(
                       lendee_id			INTEGER			PRIMARY KEY AUTO_INCREMENT,
                       first_name			VARCHAR(50),
                       last_name			VARCHAR(50),
                       alias				VARCHAR(50),
                       last_active_date 	DATE 			DEFAULT (CURRENT_DATE)
);

CREATE TABLE language(
                         language_id			INTEGER			PRIMARY KEY AUTO_INCREMENT,
                         language			VARCHAR(50)		UNIQUE
);

CREATE TABLE media(
                      media_id			INTEGER			PRIMARY KEY AUTO_INCREMENT,
                      isbn				VARCHAR(13),
                      title				VARCHAR(350)	NOT NULL,
                      original_title		VARCHAR(350),
                      cover_url			VARCHAR(255),
                      description			TEXT,
                      rating				INTEGER,
                      release_date		DATE,
                      series_order		INTEGER,
                      series_id			INTEGER,
                      mediatype_id		INTEGER,
                      publisher_id		INTEGER,
                      status				ENUM('AVAILABLE', 'LENT', 'LOST') DEFAULT 'AVAILABLE',

                      title_search		LONGTEXT,
                      refresh_t_s			TIMESTAMP,


                      CONSTRAINT chk_media_rating CHECK (rating >= 1 AND rating <= 5),

                      FOREIGN KEY (series_id) REFERENCES series (series_id),
                      FOREIGN KEY (mediatype_id) REFERENCES mediatype (mediatype_id),
                      FOREIGN KEY (publisher_id) REFERENCES publisher (publisher_id)
);

CREATE TABLE media_franchise(
                                media_franchise_id	INTEGER			PRIMARY KEY AUTO_INCREMENT,
                                media_id			INTEGER,
                                franchise_id		INTEGER,

                                FOREIGN KEY (media_id) REFERENCES media (media_id),
                                FOREIGN KEY (franchise_id) REFERENCES franchise (franchise_id)
);

CREATE TABLE alt_title(
                          alt_title_id		INTEGER			PRIMARY KEY AUTO_INCREMENT,
                          title				VARCHAR(350)	NOT NULL,
                          series_id			INTEGER			NULL,
                          franchise_id		INTEGER			NULL,

                          FOREIGN KEY (series_id) REFERENCES series (series_id) ON DELETE CASCADE,
                          FOREIGN KEY (franchise_id) REFERENCES franchise (franchise_id) ON DELETE CASCADE,

                          CONSTRAINT chk_alt_title_single_target CHECK(
                              (series_id IS NOT NULL AND franchise_id IS NULL) OR
                              (franchise_id IS NOT NULL AND series_id IS NULL)
                              )
);

CREATE TABLE media_tag(
                          media_id			INTEGER,
                          tag_id				INTEGER,

                          PRIMARY KEY (media_id, tag_id),
                          FOREIGN KEY (media_id) REFERENCES media (media_id)	ON DELETE CASCADE,
                          FOREIGN KEY (tag_id) REFERENCES tag (tag_id)		ON DELETE CASCADE
);


CREATE TABLE media_genre(
                            media_id			INTEGER,
                            genre_id			INTEGER,

                            PRIMARY KEY (media_id, genre_id),
                            FOREIGN KEY (media_id) REFERENCES media (media_id)	ON DELETE CASCADE,
                            FOREIGN KEY (genre_id) REFERENCES genre (genre_id)	ON DELETE CASCADE
);

CREATE TABLE media_language(
                               media_id			INTEGER,
                               language_id			INTEGER,

                               PRIMARY KEY (media_id, language_id),
                               FOREIGN KEY (media_id) REFERENCES media (media_id)			ON DELETE CASCADE,
                               FOREIGN KEY (language_id) REFERENCES language (language_id)	ON DELETE CASCADE
);

CREATE TABLE media_artist(
                             media_id            INTEGER,
                             artist_id           INTEGER,
                             artist_role_id      INTEGER,

                             PRIMARY KEY (media_id, artist_id, artist_role_id),
                             FOREIGN KEY (media_id) REFERENCES media (media_id) ON DELETE CASCADE,

                             # artist can only be deleted if not assigned
    FOREIGN KEY (artist_id) REFERENCES artist (artist_id) ON DELETE RESTRICT,
                             # artist_role can only be deleted if not assigned
    FOREIGN KEY (artist_role_id) REFERENCES artist_role (artist_role_id) ON DELETE RESTRICT
);

CREATE TABLE lending(
                        lending_id			INTEGER		PRIMARY KEY AUTO_INCREMENT,
                        media_id			INTEGER 	NOT NULL,
                        lendee_id			INTEGER		NULL,
                        note				VARCHAR(100),
                        borrow_date			DATE,
                        return_date			DATE,

                        CONSTRAINT chk_lending_dates CHECK (return_date IS NULL OR return_date >= borrow_date),

                        FOREIGN KEY (media_id) REFERENCES media (media_id),
                        FOREIGN KEY (lendee_id) REFERENCES lendee (lendee_id)
);



# Indexes
# Quick title search
CREATE FULLTEXT INDEX idx_media_title ON media (title_search) WITH PARSER ngram; # uses all available titles

# title search for groupings
CREATE FULLTEXT INDEX idx_alt_title_name ON alt_title (title) WITH PARSER ngram;
CREATE FULLTEXT INDEX idx_franchise_title ON franchise (franchise_name) WITH PARSER ngram;
CREATE FULLTEXT INDEX idx_series_title ON series (series_name) WITH PARSER ngram;

# standard sorters and filters
CREATE INDEX idx_media_release_date ON media (release_date);
CREATE INDEX idx_media_series_order ON media (series_order);
CREATE INDEX idx_tag_name ON tag (tag);
CREATE INDEX idx_series_name ON series (series_name);
CREATE INDEX idx_genre_name ON genre (genre_name);


# views
CREATE VIEW v_media_overview AS
SELECT
    m.media_id,
    m.title,
    m.release_date,
    p.publisher_name,
    m.status
FROM media AS m
         LEFT JOIN publisher AS p ON m.publisher_id = p.publisher_id;

CREATE VIEW v_lending_dashboard AS
SELECT DISTINCT
    l.lending_id,
    m.title AS media_title,
    -- Concatenate the exact "Lendee + alias" format for UI
    CONCAT(le.first_name, ' ', le.last_name, ' (', le.alias, ')') AS lendee_info,
    l.borrow_date AS lent_on,
    l.return_date AS returned_on,
    CASE
        WHEN m.status = 'LOST' THEN 'LOST'
        WHEN l.return_date IS NULL THEN 'ACTIVE'
        ELSE 'RETURNED'
        END AS lending_status
FROM lending AS l
         JOIN media AS m ON l.media_id = m.media_id
         JOIN lendee AS le ON l.lendee_id = le.lendee_id;


# triggers & events
SET GLOBAL event_scheduler = ON;

DELIMITER //
CREATE TRIGGER sync_media_status_on_lend
    AFTER INSERT ON lending
    FOR EACH ROW
BEGIN
    UPDATE media
    SET status = 'LENT'
    WHERE media_id = NEW.media_id;
END; //

CREATE TRIGGER sync_media_status_on_return
    AFTER UPDATE ON lending
    FOR EACH ROW
BEGIN
    IF OLD.return_date IS NULL AND NEW.return_date IS NOT NULL THEN
    UPDATE media
    SET status = 'AVAILABLE'
    WHERE media_id = NEW.media_id AND status != 'Lost';
END IF;
END; //


CREATE EVENT anonymize_old_lendings
ON SCHEDULE EVERY 1 DAY
DO
BEGIN
    # remove personal data for any medium returned more than 1 year ago
UPDATE lending
SET lendee_id = NULL, note = 'Anonymized for compliance'
WHERE return_date IS NOT NULL
  AND return_date < DATE_SUB(NOW(), INTERVAL 1 YEAR);
END;//


CREATE TRIGGER update_lendee_activity
    AFTER INSERT ON lending
    FOR EACH ROW
BEGIN
    UPDATE lendee
    SET last_active_date = CURRENT_DATE
    WHERE lendee_id = NEW.lendee_id;
END; //

CREATE TRIGGER update_lendee_activity_on_return
    AFTER UPDATE ON lending
    FOR EACH ROW
BEGIN
    IF OLD.return_date IS NULL AND NEW.return_date IS NOT NULL THEN
    UPDATE lendee
    SET last_active_date = CURRENT_DATE
    WHERE lendee_id = NEW.lendee_id;
END IF;
END; //

CREATE EVENT purge_inactive_lendees
ON SCHEDULE EVERY 1 DAY
DO
BEGIN
DELETE FROM lendee
WHERE last_active_date < DATE_SUB(CURRENT_DATE, INTERVAL 3 YEAR)
  AND lendee_id NOT IN (
    SELECT lendee_id
    FROM lending
    WHERE return_date IS NULL AND lendee_id IS NOT NULL
);
END;//

CREATE TRIGGER trg_cleanup_on_media_franchise
    AFTER DELETE on media_franchise
    FOR EACH ROW
BEGIN
    IF NOT EXISTS (SELECT 1 FROM media_franchise WHERE franchise_id = OLD.franchise_id) THEN
    DELETE FROM franchise
    WHERE franchise_id = OLD.franchise_id;
END IF;
END; //

CREATE TRIGGER validate_isbn_on_insert
    BEFORE INSERT ON media
    FOR EACH ROW
BEGIN
    IF LENGTH(NEW.isbn) != 10  AND LENGTH(NEW.isbn) != 13 AND NEW.isbn IS NOT NULL THEN
	SIGNAL SQLSTATE '45000'
	SET MESSAGE_TEXT = 'INVALID ISBN: Length must be 10 or 13';
END IF;
END;//

CREATE TRIGGER validate_isbn_on_update
    BEFORE UPDATE ON media
    FOR EACH ROW
BEGIN
    IF LENGTH(NEW.isbn) != 10  AND LENGTH(NEW.isbn) != 13 AND NEW.isbn IS NOT NULL THEN
	SIGNAL SQLSTATE '45000'
	SET MESSAGE_TEXT = 'INVALID ISBN: Length must be 10 or 13';
END IF;
END;//

# optimizing title-search performance
# ai = after insert		au= after update
                                             CREATE TRIGGER trg_media_ai_insert_search
                                             AFTER INSERT ON media
                                             FOR EACH ROW
BEGIN
CALL sp_update_title_search(NEW.media_id);
END;//

CREATE TRIGGER trg_media_au_update_search
    AFTER UPDATE ON media
    FOR EACH ROW
BEGIN
    IF NOT OLD.title <=> NEW.title
		OR NOT (OLD.original_title <=> NEW.original_title)
		OR NOT (OLD.refresh_t_s <=> NEW.refresh_t_s) THEN
		CALL sp_update_title_search(NEW.media_id);
END IF;
END;//


CREATE TRIGGER trg_series_au_update_search
    AFTER UPDATE ON series
    FOR EACH ROW
BEGIN
    IF NOT (OLD.series_name <=> NEW.series_name) THEN
    UPDATE media
    SET refresh_t_s = CURRENT_TIMESTAMP
    WHERE series_id = NEW.series_id;
END IF;
END;//

CREATE TRIGGER trg_alt_title_ai_update_search
    AFTER INSERT ON alt_title
    FOR EACH ROW
BEGIN
    IF NEW.series_id IS NOT NULL THEN
    UPDATE media SET refresh_t_s = CURRENT_TIMESTAMP WHERE series_id = NEW.series_id;
END IF;
IF NEW.franchise_id IS NOT NULL THEN
UPDATE media m
    JOIN media_franchise mf ON m.media_id = mf.media_id
    SET m.refresh_t_s = CURRENT_TIMESTAMP
WHERE mf.franchise_id = NEW.franchise_id;
END IF;
END;//



DELIMITER ;


# procedures
DELIMITER //
CREATE PROCEDURE sp_update_title_search(IN p_media_id INT)
BEGIN
UPDATE media AS m

SET m.title_search = (
    SELECT CONCAT_WS(' ',
                     m.title,
                     m.original_title,
                     (SELECT s.series_name FROM series AS s WHERE m.series_id = s.series_id),
                     (SELECT GROUP_CONCAT(a.title SEPARATOR ' ')
                      FROM alt_title AS a
                      WHERE a.series_id = m.series_id
                         OR a.franchise_id IN (SELECT franchise_id FROM media_franchise WHERE media_id = p_media_id))
           )
)
WHERE m.media_id = p_media_id;
END;//

CREATE PROCEDURE src_media_by_title(IN p_search_term VARCHAR(255))
BEGIN
    # search main title
SELECT
    m.media_id,
    m.title,
    m.original_title,
    m.release_date,
    p.publisher_name
FROM media AS m
         LEFT JOIN publisher AS p ON m.publisher_id = p.publisher_id
WHERE MATCH(m.title_search) AGAINST(p_search_term IN BOOLEAN MODE);
END; //

CREATE PROCEDURE src_artist_by_name(IN p_search_term VARCHAR(50))
BEGIN
SELECT
    a.artist_id,
    a.first_name,
    a.last_name,
    a.nationality
FROM artist AS a
WHERE a.first_name LIKE CONCAT(p_search_term, '%') OR a.last_name LIKE  CONCAT(p_search_term, '%');
END; //


DELIMITER ;
