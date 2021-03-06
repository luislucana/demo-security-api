package br.com.exemplo.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfigurations extends WebSecurityConfigurerAdapter {

    @Autowired
    private AutenticacaoService autenticacaoService;

    @Override
    @Bean // para que seja possivel injeta-lo no controller
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    /**
     * Configuracoes de autenticacao
     *
     * Devemos indicar ao Spring Security qual o algoritmo de hashing de
     * senha que utilizaremos na API, chamando o método passwordEncoder(),
     * dentro do método configure(AuthenticationManagerBuilder auth),
     * que está na classe SecurityConfigurations.
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(autenticacaoService).passwordEncoder(new BCryptPasswordEncoder());
    }

    // configuracoes de autorizacao
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/topicos").permitAll()
            .antMatchers(HttpMethod.GET, "/topicos/*").permitAll()
            .antMatchers(HttpMethod.POST, "/auth").permitAll()
            .and().authorizeRequests().antMatchers( "/h2-console", "/h2-console/**").permitAll()
            .anyRequest().authenticated()
            .and().sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // politica de criacao de sessao
                                                                    // (nao desejo criar sessao)
            // executar o filtro AutenticacaoViaTokenFilter antes do filtro padrao do Spring (UsernamePasswordAuthenticationFilter)
            .and().addFilterBefore(new AutenticacaoViaTokenFilter(), UsernamePasswordAuthenticationFilter.class);
            //.and().formLogin(); // formLogin() diz ao spring para gerar um formulario de autenticacao
                                // e um Controller que recebe as requisicoes desse formulario (sessao)
                                // http://localhost:8080/login
        http.headers().frameOptions().disable();
    }

    // configuracoes de recursos estaticos (javascript, css, imagens, etc...)
    @Override
    public void configure(WebSecurity web) throws Exception {
    }
}
