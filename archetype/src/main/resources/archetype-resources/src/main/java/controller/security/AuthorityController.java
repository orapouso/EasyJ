package ${package}.controller.security;

import ${package}.domain.security.Authority;
import org.easyj.rest.controller.jpa.JPAGenericEntityController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/authority")
public class AuthorityController extends JPAGenericEntityController<Authority, String>{
    
}