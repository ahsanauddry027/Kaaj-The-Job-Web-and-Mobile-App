package app.kaaj.seeker.internal;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SeekerProfileRepository extends JpaRepository<SeekerProfile, UUID> {
}
