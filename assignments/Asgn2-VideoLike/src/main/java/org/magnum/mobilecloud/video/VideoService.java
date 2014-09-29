package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

/**
 * This simple VideoSvc allows clients to send HTTP POST requests with
 * videos that are stored in memory using a list. Clients can send HTTP GET
 * requests to receive a JSON listing of the videos that have been sent to
 * the controller so far. Stopping the controller will cause it to lose the history of
 * videos that have been sent to it because they are stored in memory.
 * 
 * Notice how much simpler this VideoSvc is than the original VideoServlet?
 * Spring allows us to dramatically simplify our service. Another important
 * aspect of this version is that we have defined a VideoSvcApi that provides
 * strong typing on both the client and service interface to ensure that we
 * don't send the wrong paraemters, etc.
 * 
 * @author jules
 *
 */

// Tell Spring that this class is a Controller that should 
// handle certain HTTP requests for the DispatcherServlet
@Controller
public class VideoService {
    
    @Autowired
    private VideoRepository videos;

    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
    public @ResponseBody Video addVideo(@RequestBody Video v){
         videos.save(v);
         return v;
    }
    
    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideoList(){
        return Lists.newArrayList(videos.findAll());
    }
    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/like", method=RequestMethod.POST)
    public ResponseEntity<Void> likeVideo(@PathVariable("id") long id, Principal p) {
            String username = p.getName(); 
            if (!videos.exists(id))
            {
                return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
            }
            Video v = videos.findOne(id);
            if (v == null)
            {
                return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
            }
            else
            {
                if (v.hasUserLiked(username))
                {
                    return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                }
                
                v.addToUsersWhoLikedThis(username);
                v.setLikes(v.getUsersWhoLikedThis().size());
                videos.save(v);
                return new ResponseEntity<Void>(HttpStatus.OK);
            }
    }
    
    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/unlike", method=RequestMethod.POST)
    public ResponseEntity<Void> unlikeVideo(@PathVariable("id") long id, Principal p) {
            String username = p.getName();
            if (!videos.exists(id))
            {
                return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
            }
            Video v = videos.findOne(id);
            if (v == null)
            {
                return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
            }
            else
            {
                if (!v.hasUserLiked(username))
                {
                    return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                }
                
                v.removeUserWhoLiked(username);
                v.setLikes(v.getUsersWhoLikedThis().size());
                videos.save(v);
                return new ResponseEntity<Void>(HttpStatus.OK);
            }
    }
    
    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/likedby", method=RequestMethod.GET)
    public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
            String username = p.getName(); 
            if (!videos.exists(id))
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            Video v = videos.findOne(id);
            if (v == null)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            
            return v.getUsersWhoLikedThis();
            
        
    }
    
    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}", method=RequestMethod.GET)
    public @ResponseBody Video getVideoById(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
            String username = p.getName(); 
            if (!videos.exists(id))
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            Video v = videos.findOne(id);
            if (v == null)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            
            return v;
            
        
    }
}
