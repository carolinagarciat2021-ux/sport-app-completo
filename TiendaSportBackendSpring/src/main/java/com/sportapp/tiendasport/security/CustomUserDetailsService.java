package com.sportapp.tiendasport.security;

import com.sportapp.tiendasport.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        return clienteRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un cliente con el correo: " + correo));
    }
}
