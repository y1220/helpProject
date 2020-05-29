package it.course.helpProject.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@AuthenticationPrincipal
public @interface CurrentUser {

}
