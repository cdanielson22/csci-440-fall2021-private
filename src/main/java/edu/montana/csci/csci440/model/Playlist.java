package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Playlist extends Model {

    Long playlistId;
    String name;

    public Playlist() {
    }

    private Playlist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        playlistId = results.getLong("PlaylistId");
    }

    public static List<Playlist> getPlayForTrack(ResultSet results) throws SQLException{
        List<Playlist> resultList = new LinkedList<>();
        while (results.next()) {
            resultList.add(new Playlist(results));
        }
        return resultList;
    }

    // this method gets the tracks using the playlistId to get all tracks in a playlist
    public List<Track> getTracks(){
        // TODO implement, order by track name
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM tracks " +
                             " JOIN playlist_track pt on tracks.TrackId = pt.TrackId " +
                             " JOIN playlists p on p.PlaylistId = pt.PlaylistId " +
                             " WHERE p.PlaylistId = "+playlistId+" ORDER BY tracks.Name "
             )) {
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = Track.getTracksForPlay(results);
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

    }

    public Long getPlaylistId() {
        return playlistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Playlist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    // this method lists all the playlists and it has paging implemented in it
    public static List<Playlist> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM playlists LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count * page - count);
            ResultSet results = stmt.executeQuery();
            List<Playlist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Playlist(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // this method is used to get all the values from a playlist
    public static Playlist find(int i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM playlists WHERE PlaylistId=?")) {
            stmt.setLong(1, i);

            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Playlist(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

}
