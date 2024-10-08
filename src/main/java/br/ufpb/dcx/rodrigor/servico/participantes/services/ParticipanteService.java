package br.ufpb.dcx.rodrigor.servico.participantes.services;

import br.ufpb.dcx.rodrigor.servico.AbstractService;
import br.ufpb.dcx.rodrigor.servico.db.MongoDBRepository;
import br.ufpb.dcx.rodrigor.servico.participantes.model.CategoriaParticipante;
import br.ufpb.dcx.rodrigor.servico.participantes.model.Participante;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

public class ParticipanteService extends AbstractService {

    private final MongoCollection<Document> collection;

    public ParticipanteService(MongoDBRepository mongoDBRepository) {
        super(mongoDBRepository);
        MongoDatabase database = mongoDBRepository.getDatabase("projetos");
        this.collection = database.getCollection("participantes");
    }

    public List<Participante> listarParticipantesPorCategoria(CategoriaParticipante categoriaParticipante) {
        List<Participante> participantes = new LinkedList<>();
        for (Document doc : collection.find(eq("categoria", categoriaParticipante.name()))) {
            participantes.add(documentToParticipante(doc));
        }
        return participantes;
    }

    public List<Participante> listarProfessores() {
        return listarParticipantesPorCategoria(CategoriaParticipante.PROFESSOR);
    }

    public List<Participante> listarParticipantes() {
        List<Participante> participantes = new LinkedList<>();
        for (Document doc : collection.find()) {
            participantes.add(documentToParticipante(doc));
        }
        return participantes;
    }



    public Optional<Participante> buscarParticipantePorId(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }
        Document doc = collection.find(eq("_id", new ObjectId(id))).first();
        return Optional.ofNullable(doc).map(ParticipanteService::documentToParticipante);
    }

    public void adicionarParticipante(Participante participante) {
        Document doc = participanteToDocument(participante);
        collection.insertOne(doc);
        participante.setId(doc.getObjectId("_id").toString());
    }


    public void removerParticipante(String id) {
        collection.deleteOne(eq("_id", new ObjectId(id)));
    }

    public static Participante documentToParticipante(Document doc) {
        if(doc == null) {
            return null;
        }
        Participante participante = new Participante();
        participante.setId(doc.getObjectId("_id").toString());
        participante.setNome(doc.getString("nome"));
        participante.setSobrenome(doc.getString("sobrenome"));
        participante.setEmail(doc.getString("email"));
        participante.setTelefone(doc.getString("telefone"));
        participante.setCategoria(CategoriaParticipante.valueOf(doc.getString("categoria")));
        return participante;
    }

    public static Document participanteToDocument(Participante participante) {
        Document doc = new Document();
        if (participante.getId() != null) {
            doc.put("_id", participante.getId());
        }
        doc.put("nome", participante.getNome());
        doc.put("sobrenome", participante.getSobrenome());
        doc.put("email", participante.getEmail());
        doc.put("telefone", participante.getTelefone());
        doc.put("categoria", participante.getCategoria().name());
        return doc;
    }
}