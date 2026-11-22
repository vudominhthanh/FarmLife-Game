package repository;

import entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findAllByPlayer_PlayerId(UUID playerId);
    Optional<Device> findByDeviceIdAndPlayer_PlayerId(String deviceId, UUID playerId);
}
