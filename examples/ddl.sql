

CREATE TABLE albums_bak (
    AlbumId  INTEGER,
    Title    NVARCHAR(160),
    ArtistId INTEGER
);

--INSERT INTO grammy_categories(Name) VALUES ('Greatest Ever');

CREATE TABLE grammy_categories(
    Name,
    GrammyCatagoryId
);
DROP TABLE grammy_categories;

--"INSERT INTO grammy_infos(ArtistId, AlbumId, TrackId, GrammyCategoryId, Status)
-- VALUES (1, 1, 1, " + categoryId + ",'Won');"


DROP TABLE albums_bak;


DROP TABLE albums_backup;


-- Proof SQLite doens't care...
INSERT INTO albums (Title, ArtistId) VALUES ("Lopado­temacho­selacho­galeo­kranio­leipsano­drim­hypo­trimmato­silphio­karabo­melito­katakechy­meno­kichl­epi­kossypho­phatto­perister­alektryon­opte­kephallio­kigklo­peleio­lagoio­siraio­baphe­tragano­pterygon", 1);

ALTER TABLE
albums_bak
RENAME TO albums_backup;

ALTER TABLE
albums_backup
ADD COLUMN NEW_COL TEXT;

ALTER TABLE
albums_backup
RENAME COLUMN NEW_COL to NewColumn;

ALTER TABLE
albums_backup
DROP COLUMN NewColumn;

CREATE VIEW tracksPlus AS
SELECT tracks.*,
       albums.Title as AlbumTitle,
       artists.Name as ArtistName
FROM tracks
         JOIN albums ON
             tracks.AlbumId = albums.AlbumId
         JOIN artists ON
             albums.ArtistId = artists.ArtistId;

SELECT *
from tracksPlus
WHERE ArtistName = "AC/DC";

DROP VIEW tracksPlus;


CREATE VIEW tracksPlus AS
SELECT artists.Name as ArtistName,
       tracks.TrackId as TrackId,
       albums.Title as AlbumTitle,
       genres.Name as Genre
FROM tracks
        JOIN albums ON tracks.AlbumId = albums.AlbumId
        JOIN artists ON albums.ArtistId = artists.ArtistId
        JOIN genres ON tracks.GenreId = genres.GenreId;
