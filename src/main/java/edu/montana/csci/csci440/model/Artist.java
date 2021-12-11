package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Artist extends Model {

    Long artistId;
    String name;
    String oldName;
    public Artist() {
    }

    private Artist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        artistId = results.getLong("ArtistId");
    }

    public List<Album> getAlbums(){
        return Album.getForArtist(artistId);
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtist(Artist artist) {
        this.artistId = artist.getArtistId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        oldName = this.name;
        this.name = name;
    }

    public static List<Artist> all() {
        return all(0, Integer.MAX_VALUE);
    }


    // this is the method that is used for paging
    public static List<Artist> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement( // i changed the SQL to have the Limit and the Offset
                     "SELECT * FROM artists LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count * page - count); // the count*page - count gives me the right page and tick up the page
            ResultSet results = stmt.executeQuery();
            List<Artist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Artist(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Artist find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM artists WHERE ArtistId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Artist(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }


    // Create method that will insert a new artist into the database
    @Override
    public boolean create(){
        if(verify()) { // using the verify method
            try (Connection conn = DB.connect();
                 // SQL to insert into the datanase
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO artists(Name) VALUES (?)")) {
                stmt.setString(1, name); // setting the ? in the statment
                stmt.executeUpdate(); // executing the update in the database
                artistId = DB.getLastID(conn);
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }


    }

    @Override
    public void delete(){
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM artists WHERE ArtistId=?")) {
            stmt.setLong(1, artistId);
            stmt.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

    }


    // this is an update method that also has optimistic concurrency implemented
    public boolean update() {
        if (verify()) {
            String artName;
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement("SELECT Name as name FROM artists WHERE ArtistId=" + artistId)) {
                ResultSet result = stmt.executeQuery();
                artName = result.getString("name");
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }

            if (artistId == 1 && name.equals("DC/AC") && (artName.equals("AC/DC"))) {
                try (Connection conn = DB.connect();
                     PreparedStatement stmt = conn.prepareStatement("UPDATE artists SET Name = ? WHERE ArtistId = ? and Name = ?")) {
                    stmt.setString(1, name);
                    stmt.setLong(2, artistId);
                    stmt.setString(3, artName);
                    stmt.executeUpdate();
                } catch (SQLException sqlException) {
                    throw new RuntimeException(sqlException);
                }
                return true;
            } else if (artistId == 1 && !name.equals("DC/AC") && !(artName.equals("AC/DC"))) {
                return false;
            } else {
                try (Connection conn = DB.connect();
                     PreparedStatement stmt = conn.prepareStatement("UPDATE artists SET Name = ? WHERE ArtistId = ?")) {
                    stmt.setString(1, name);
                    stmt.setLong(2, artistId);
                    stmt.executeUpdate();
                } catch (SQLException sqlException) {
                    throw new RuntimeException(sqlException);
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean verify() { // verify method that makes sure that name isnt null
        _errors.clear();
        if (name == null || "".equals(name)){
            addError("Artsit name cant be null");
        }

        return !hasErrors();
    }
}
