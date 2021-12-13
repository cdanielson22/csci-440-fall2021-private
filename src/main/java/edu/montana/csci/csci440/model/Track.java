package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track extends Model {

    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;
    private String albumTitle;
    private String artistName;

    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";


    public Track() {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");
    }

    // I make a second constructor for the having the album title and artist name on tracks
    // I need to keep the other constructor because ths is only used in some cases
    private Track(ResultSet results, int i) throws SQLException {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");
        albumTitle = results.getString("Album");
        artistName = results.getString("ArtistName");

    }

    private Track(ResultSet results) throws SQLException {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");
    }

    public static List<Track> getTracksForPlay(ResultSet results) throws SQLException{
        List<Track> resultList = new LinkedList<>();
        while (results.next()) {
            resultList.add(new Track(results));
        }
        return resultList;
    }

    // this method finds a track from tracks and also joins the
    // artist and ablum tables and stroes them in tracks
    public static Track find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT *, al.Title as Album, at.Name as ArtistName FROM tracks " +
                     "JOIN albums al on tracks.AlbumId = al.AlbumId " +
                     "JOIN artists at on al.ArtistId = at.ArtistId " +
                     "WHERE TrackId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Track(results, 1); // I call the constructor that I made to
                                                // store the extra values
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // this method gets the count of the tracks and stores it with redis
    public static Long count() {
        Jedis redisClient = new Jedis(); // use this class to access redis and create a cache
        String cache = redisClient.get(REDIS_CACHE_KEY);
        // i check if the cache is null
        // the cache gets cleared every time create or delete is called
        if(cache == null) { // If it is I connect and get a count then store in redis and recall the function
            try(Connection conn = DB.connect();
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as Count FROM tracks")) {
                ResultSet results = stmt.executeQuery();
                if(results.next()){ // make sure that results isnt null
                    // Here I set eh value in redis
                    redisClient.set(REDIS_CACHE_KEY, Long.toString(results.getLong("Count")));

                    return Track.count(); // this is the recursive call is here
                } else {
                    throw new IllegalStateException("Should find a count!");
                }

            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }

        } else { // If the cache isn't null I return the value in the cache
              return Long.parseLong(cache);
        }

    }

    public Album getAlbum() {
        return Album.find(albumId);
    }

    public MediaType getMediaType() {
        return null;
    }
    public Genre getGenre() {
        return null;
    }

    // this method gets the playlists were the current track is in the playlist
    public List<Playlist> getPlaylists(){
        try(Connection conn = DB.connect();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM playlists " +
                            "JOIN playlist_track pt on playlists.PlaylistId = pt.PlaylistId " +
                            " JOIN tracks t on pt.TrackId = t.TrackId " +
                            " WHERE t.TrackId = "+trackId+" ORDER BY playlists.PlaylistId"

            )) {
            ResultSet results = stmt.executeQuery();
            List<Playlist> resultList = Playlist.getPlayForTrack(results);

            return resultList;

        }catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }


    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(Album album) {
        albumId = album.getAlbumId();
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    // This methods get the artist thats on tracks
    public String getArtistName() {
        // TODO implement more efficiently
        //  hint: cache on this model object
        return artistName;
    }

    // This gets the album thats stored on tracks
    public String getAlbumTitle() {
        // TODO implement more efficiently
        //  hint: cache on this model object

        return albumTitle;
    }

    // this is the advanced search method
    public static List<Track> advancedSearch(int page, int count,
                                             String search, Integer artistId, Integer albumId,
                                             Integer maxRuntime, Integer minRuntime) {
        LinkedList<Object> args = new LinkedList<>();

        String query = "SELECT * FROM tracks " +
                "JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                "WHERE name LIKE ?";
        args.add("%" + search + "%");


        // Conditionally include the query and argument
        if (artistId != null) {
            query += " AND ArtistId=? ";
            args.add(artistId);
        }

        // if the parameters arent null then I add to the query and then the value to args
        // the check is done three times for all the parameter
        if(albumId != null){
            query += " AND tracks.AlbumId=? ";
            args.add(albumId);
        }

        if(maxRuntime != null){
            query += " AND Milliseconds<?";
            args.add(maxRuntime*1000);
        }

        if(minRuntime != null){
            query += " AND Milliseconds>?";
            args.add(minRuntime*1000);
        }

        // I then implement paging for the search
        query += " LIMIT ? OFFSET ?";
        args.add(count);
        args.add(count * page - count);

        // The query is then executed and the results list is returned
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                stmt.setObject(i + 1, arg);
            }
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // here the simple search is implemented
    public static List<Track> search(int page, int count, String orderBy, String search) {
        // making the query outside the try catch
        String query = "SELECT *, al.Title as Album, at.Name as ArtistName FROM tracks " +
                "JOIN albums al on tracks.AlbumId = al.AlbumId " +
                "JOIN artists at on al.ArtistId = at.ArtistId " +
                "WHERE tracks.name LIKE ? OR al.Title LIKE ? OR at.Name LIKE ? LIMIT ? OFFSET ?";
        search = "%" + search + "%";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) { // making a connection to the databate and executing the query
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            stmt.setInt(4, count);
            stmt.setInt(5, count * page - count);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>(); // returning the results list from the query
            while (results.next()) {
                resultList.add(new Track(results, 1));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // this method gets the tracks for the albums and is given an album id
    public static List<Track> forAlbum(Long albumId) {
        String query = "SELECT * FROM tracks WHERE AlbumId=?";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, albumId);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // Sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        return all(page, count, "TrackId");
    }

    // this method gets all the tracks from the database
    // paging is also implemented here aswell
    public static List<Track> all(int page, int count, String orderBy) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tracks.*, al.Title AS Album, at.Name AS ArtistName FROM tracks " +
                             "JOIN albums al on tracks.AlbumId = al.AlbumId " +
                             "JOIN artists at on al.ArtistId = at.ArtistId " +
                             "ORDER BY "+orderBy+" LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count * page - count);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results, 1));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // this method updates a selected track in the database
    public boolean update(){
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tracks SET Name = ? WHERE main.tracks.TrackId = ?")){
            stmt.setString(1, this.name);
            stmt.setLong(2, trackId);
            stmt.executeUpdate();
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }

        return true;
    }

    // This method creates a new track in the database
    @Override
    public boolean create() {
        if(verify()) {
            Jedis redis = new Jedis();
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO tracks(Name, MediaTypeId, GenreId, Milliseconds, UnitPrice, Bytes, AlbumId) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, name);
                stmt.setLong(2, this.mediaTypeId);
                stmt.setLong(3, this.genreId);
                stmt.setLong(4, this.milliseconds);
                stmt.setBigDecimal(5, this.unitPrice);
                stmt.setLong(6, this.bytes);
                stmt.setLong(7, this.albumId);


                stmt.executeUpdate();
                trackId = DB.getLastID(conn);


            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }

            redis.flushDB(); // i clear redis because the count is no longer good
            // the cache with now be null so when count is called again it will get a new count
        }
        return true;

    }

    // this method is used to delete a track from the database
    public void delete(){
        if(verify()) {
            Jedis redis = new Jedis();
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM tracks WHERE TrackId=?")) {
                stmt.setLong(1, trackId);
                stmt.executeUpdate();
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
            redis.flushDB(); // I clear the redis count so it null and when count is called it will get a need count
        }
    }

    // this method verifys that the values thata re given aren't null and can be inserted in to the database
    @Override
    public boolean verify() {
        _errors.clear();
        if (name == null || name.equals("")){
            addError("name cant be null");
        }
        if(bytes == null || "".equals(Long.toString(bytes))){
            addError("bytes cant be null");
        }
        if(genreId == null || "".equals(Long.toString(genreId))){
            addError("Genre cant be null");
        }
        if(mediaTypeId == null || "".equals(Long.toString(mediaTypeId))){
            addError("media type cant be null");
        }
        if(milliseconds == null || "".equals(Long.toString(milliseconds))){
            addError("milliseconds cant be null");
        }
        if(unitPrice == null || unitPrice.toString().equals("")){
            addError("unit price cant be null");
        }
        if (albumId == null || "".equals(Long.toString(albumId))) {
            addError("albumId cant be null");
        }

        return !hasErrors();
    }
}
