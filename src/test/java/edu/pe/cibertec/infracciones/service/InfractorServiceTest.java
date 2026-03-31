package edu.pe.cibertec.infracciones.service;

import edu.pe.cibertec.infracciones.model.EstadoMulta;
import edu.pe.cibertec.infracciones.model.Infractor;
import edu.pe.cibertec.infracciones.model.Multa;
import edu.pe.cibertec.infracciones.model.Vehiculo;
import edu.pe.cibertec.infracciones.repository.InfractorRepository;
import edu.pe.cibertec.infracciones.repository.MultaRepository;
import edu.pe.cibertec.infracciones.repository.VehiculoRepository;
import edu.pe.cibertec.infracciones.service.impl.InfractorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InfractorServiceTest {

    @Mock
    private InfractorRepository infractorRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private MultaRepository multaRepository;

    @InjectMocks
    private InfractorServiceImpl infractorService;

    @Test
    void calcularDeuda_conMultasPendientesYVencidas_retornaDeudaTotal() {
        Infractor infractor = new Infractor();
        infractor.setId(1L);

        Multa multaPendiente = new Multa();
        multaPendiente.setMonto(200.00);
        multaPendiente.setEstado(EstadoMulta.PENDIENTE);

        Multa multaVencida = new Multa();
        multaVencida.setMonto(300.00);
        multaVencida.setEstado(EstadoMulta.VENCIDA);

        when(infractorRepository.findById(1L)).thenReturn(Optional.of(infractor));
        when(multaRepository.findByInfractor_IdAndEstado(1L, EstadoMulta.PENDIENTE))
                .thenReturn(List.of(multaPendiente));
        when(multaRepository.findByInfractor_IdAndEstado(1L, EstadoMulta.VENCIDA))
                .thenReturn(List.of(multaVencida));

        Double deuda = infractorService.calcularDeuda(1L);

        assertEquals(545.00, deuda);
    }

    @Test
    void desasignarVehiculo_sinMultasPendientes_remueleVehiculoYGuarda() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1L);
        vehiculo.setPlaca("ABC-123");
        vehiculo.setSuspendido(false);

        Infractor infractor = new Infractor();
        infractor.setId(1L);
        infractor.setVehiculos(new ArrayList<>(List.of(vehiculo)));

        when(infractorRepository.findById(1L)).thenReturn(Optional.of(infractor));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(multaRepository.findByInfractor_IdAndVehiculo_IdAndEstado(1L, 1L, EstadoMulta.PENDIENTE))
                .thenReturn(List.of());

        infractorService.desasignarVehiculo(1L, 1L);

        assertFalse(infractor.getVehiculos().contains(vehiculo));
        verify(infractorRepository).save(infractor);
    }
}
