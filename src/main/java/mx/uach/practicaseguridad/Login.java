package mx.uach.practicaseguridad;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import mx.uach.practicaseguridad.models.Usuario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * Clase controladora para registrar un usuario e iniciar sesión.
 *
 * @author Ing. Luis Antonio Ramirez Martinez
 */
public class Login {

    private static final Logger LOGGER = LogManager.getLogger("Login");
    
    
    public static void main(String[] args) {
       
        /**
         * Ruta inicial de la app muestra el formulario de registro de la app.
         */
        get("/", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            String validador = "";
            String nombre = "";
            String mensaje = "";
            
            attributes.put("validador", validador);
            attributes.put("nombre", nombre);
            attributes.put("mensaje", mensaje);
            
            return new ModelAndView(attributes, "registrar.ftl");
        }, new FreeMarkerEngine());

        /**
         * Ruta para procesar el registro enviado desde la ruta inicial.
         */
        post("/registrar", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            
            String nombre = req.queryParams("nombre");
            String email = req.queryParams("email");
            String password = req.queryParams("password");
            String apellidos = req.queryParams("apellidos");
            String validador = "";
            String mensaje = "";
            
            if(nombre.equals("")){
                //Falta el usuario
                validador = "Por favor complete el campo nombre";
                attributes.put("nombre",nombre);
            }else if (apellidos.equals("")){
                validador = "Por favor complete el campo apellidos";
            }else if (email.equals("")){
                validador = "Por favor complete el campo email";
            }else if (password.equals("")){
                validador = "Por favor complete el campo password";
            }
            
            
            try{
                
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("PracticaSeguridadPU");
                
                EntityManager em = emf.createEntityManager();
                
                Usuario usuario = new Usuario(email,nombre,apellidos,password);             
                em.getTransaction().begin();                
                em.persist(usuario);                
                em.getTransaction().commit();             
                em.close();
                mensaje="Registro ha sido exitoso";
                        
            }catch(Exception ex){
                LOGGER.error(String.format("El usuario %s no se ha podido registrar. %s",email,ex.getMessage()));
                validador = "No se pudo registrar el usuario";
                
            }
               
            attributes.put("validador", validador);
            attributes.put("nombre", "");
            attributes.put("mensaje", mensaje);
            //TODO Guardar en Base de datos.
            return new ModelAndView(attributes, "registrar.ftl");
        }, new FreeMarkerEngine());
        
        /**
         * Ruta para verificar el inicio de sesión.
         */
        post("/login", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            String email = req.queryParams("email");
            String password = req.queryParams("password");
            
            String validador = "";
            String nombre = "";
            String mensaje = "";
            
            String usuario = "n/a";
            
            attributes.put("validador", validador);
            attributes.put("nombre", nombre);
            
            try{
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("PracticaSeguridadPU");
                
                EntityManager em = emf.createEntityManager();
                
                Query q = em.createQuery("SELECT u from Usuario u WHERE u.email=:arg1");
                q.setParameter("arg1", email);
                
                Usuario usuarioToValidate = (Usuario) q.getSingleResult();
                
                if(usuarioToValidate.getPassword().equals(usuarioToValidate.hashPassword(password))){
                LOGGER.trace(String.format("El usuario %s ha iniciado sesión.", email));
                usuario = email;
                attributes.put("mensaje", mensaje);
                attributes.put("usuario", usuario);
                return new ModelAndView(attributes, "home.ftl");
            }else{
                mensaje = "Usuario y/o Password son incorrectos.";
                LOGGER.error(String.format("password en BD %s y el mandado es %s - %s",usuarioToValidate.getPassword(),password,usuarioToValidate.hashPassword(password)));

            }
                
                em.close();
            }catch(Exception ex){
             mensaje = "Usuario y/o Password son incorrectos.";         
           LOGGER.error(String.format("%s",ex.getMessage()));

            }
            
            
            attributes.put("mensaje", mensaje);
            
            return new ModelAndView(attributes, "registrar.ftl");
        }, new FreeMarkerEngine());
    }

}
