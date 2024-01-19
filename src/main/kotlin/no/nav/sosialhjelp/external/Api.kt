package no.nav.sosialhjelp.external

import kotlinx.serialization.Serializable

@Serializable
data class FiksKommuneResponse(
    val behandlingsansvarlig: String? = null,
    val harMidlertidigDeaktivertMottak: Boolean,
    val harMidlertidigDeaktivertOppdateringer: Boolean,
    val harNksTilgang: Boolean,
    val kanMottaSoknader: Boolean,
    val kanOppdatereStatus: Boolean,
    val kommunenummer: String,
    val kontaktpersoner: Kontaktpersoner
)

@Serializable
data class Kontaktpersoner(
    val fagansvarligEpost: List<String>,
    val tekniskAnsvarligEpost: List<String>
)

@Serializable
data class GeodataKommuneResponse(
    val kommunenavn: String,
    val kommunenavnNorsk: String,
    val kommunenummer: String,
    val fylkesnavn: String? = null,
)

@Serializable
data class GeodataKommuneSearchResponse(
    val antallTreff: Int,
    val kommuner: List<GeodataKommuneResponse>
)
