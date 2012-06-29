package ${package}.controller.security;

import ${package}.domain.security.Authority;
import ${package}.domain.security.User;
import java.util.List;
import org.easyj.rest.controller.jpa.JPAGenericEntityController;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/user")
public class UserController extends JPAGenericEntityController<User, Short>{ 

    @ModelAttribute("auths")
    protected List<Authority> auths() {
        return getService().findAll(Authority.class);
    }
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(List.class, "authorities", new CustomCollectionEditor(List.class) {
            @Override
            protected Object convertElement(Object element) {
              if(element == null || "".equals((String)element)) {
                  return null;
              }
              return new Authority((String)element);
            }
        });
    }
}