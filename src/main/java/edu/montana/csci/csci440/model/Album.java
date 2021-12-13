package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Album extends Model {

    Long albumId;
    Long artistId;
    String title;

    public Album() {
    }

    private Album(ResultSet results) throws SQLException {
        title = results.getString("Title");
        albumId = results.getLong("AlbumId");
        artistId = results.getLong("ArtistId");
    }


    public Artist getArtist() {
        return Artist.find(artistId);
    }

    public void setArtist(Artist artist) { artistId = artist.getArtistId(); }

    public void setArtistId(Long artist) {artistId = artist;}

    public List<Track> getTracks() {
        return Track.forAlbum(albumId);
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbum(Album album) {
        this.albumId = album.getAlbumId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public Long getArtistId() {
        return artistId;
    }

    public static List<Album> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Album> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement( // below paging is implemented
                     "SELECT * FROM albums LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            // I do the count*page to get the pages a do - count to get back to the first page
            stmt.setInt(2, count * page - count);
            ResultSet results = stmt.executeQuery(); // get the results
            List<Album> resultList = new LinkedList<>(); // put the results in a list that can be returned
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // this method is used to find an albums basied off the albumId
    public static Album find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums WHERE AlbumId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Album(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // This method is used to get all the albums for an artist
    // uses that artist id to get all from albums with a join on the two tables
    public static List<Album> getForArtist(Long artistId) {
        // TODO implement
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums " +
                     "JOIN artists ON albums.ArtistId = artists.ArtistId " +
                     "WHERE albums.ArtistId=?")) {
            stmt.setLong(1, artistId);
            ResultSet results = stmt.executeQuery();
            List<Album> resultList = new LinkedList<>(); // put the results in a list that can be returned
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
        //return Collections.emptyList();
    }

    // This method inserts a new ablum into the database
    @Override
    public boolean create(){ // create method
        if(verify()) { // verify that ablumId not null
            try (Connection conn = DB.connect(); // connection to the database
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO albums(Title, ArtistId) VALUES (?, ?)")) { // query to insert into the DB
                stmt.setString(1, this.title); // setting the two values that are the ? in the query
                stmt.setLong(2, artistId);
                stmt.executeUpdate(); // execute the update
                albumId = DB.getLastID(conn);
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }


    }

    // This method is used to update an album in the database
    public boolean update(){ // method to update a row in the table
        if(verify()) { // verifying that albumId is not null
            try (Connection conn = DB.connect(); // connection to the database
                 PreparedStatement stmt = conn.prepareStatement("UPDATE albums SET Title = ? WHERE ArtistId = ?")) { // query to update a row
                stmt.setString(1, title); // setting the values of the ? in the query
                stmt.setLong(2, artistId);
                stmt.executeUpdate(); // execute the update
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        }

        return true;
    }

    // this method is used to delete an album from the database
    @Override
    public void delete(){
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM albums WHERE AlbumId=?")) {
            stmt.setLong(1, albumId);
            stmt.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public boolean verify(){ // verify method that clears any current errors and makes sure that title and id are non null values
        _errors.clear();

        if(artistId == null || "".equals(Long.toString(artistId))) {
            addError("artistId cant be null");
        }
        if (title == null || "".equals(title)){
            addError("title cant be null");
        }

        return !hasErrors();
    }
}
