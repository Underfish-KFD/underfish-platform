package ru.underfish.locations_service.database.dao

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import ru.underfish.locations_service.database.entities.AbstractEntity

@NoRepositoryBean
interface AbstractRepository<T : AbstractEntity> : JpaRepository<T, Long> {

}
