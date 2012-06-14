#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ForwarderController {

    @RequestMapping(value="/{whatever}", method=RequestMethod.GET)
    public ModelAndView whatever(ModelAndView mav, @PathVariable("whatever") String whatever) {
        return whateverPage(mav, whatever, "index");
    }

    @RequestMapping(value="/{whatever}/{page}", method=RequestMethod.GET)
    public ModelAndView whateverPage(ModelAndView mav, @PathVariable("whatever") String whatever, @PathVariable("page") String page) {
        mav.setViewName("/" + whatever + "/" + page);
        return mav;
    }
}