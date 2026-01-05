const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

// Inicializar Firebase Admin
admin.initializeApp();

// Configurar el transportador de correo (Gmail)
const gmailTransport = nodemailer.createTransporter({
  service: "gmail",
  auth: {
    user: "tu_correo@gmail.com", // Cambiar por tu correo
    pass: "tu_contraseña_de_app", // Cambiar por tu contraseña de aplicación
  },
});

// Función para notificar aprobación de usuario
exports.notifyUserApproval = functions
  .region("us-central1")
  .https.onCall(async (data, context) => {
    try {
      const { email, token, userName, role } = data;

      if (!email || !token) {
        throw new functions.https.HttpsError(
          "invalid-argument",
          "Email y token son requeridos"
        );
      }

      // Enviar correo de aprobación
      await gmailTransport.sendMail({
        from: "NexoGo <tu_correo@gmail.com>",
        to: email,
        subject: "✅ Tu cuenta NexoGo ha sido aprobada",
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <div style="background: linear-gradient(135deg, #1976d2, #42a5f5); padding: 20px; text-align: center;">
              <h1 style="color: white; margin: 0;">🐾 NexoGo</h1>
              <p style="color: white; margin: 10px 0 0 0;">Clínica Veterinaria</p>
            </div>
            <div style="padding: 30px; background: #f8f9fa;">
              <h2 style="color: #1976d2;">¡Buenas noticias, ${userName || "Usuario"}!</h2>
              <p style="font-size: 16px; line-height: 1.6; color: #333;">
                Tu cuenta en NexoGo ha sido <strong>aprobada exitosamente</strong>.
              </p>
              <p style="font-size: 16px; line-height: 1.6; color: #333;">
                <strong>Rol:</strong> ${getRoleDisplayName(role)}<br>
                <strong>Estado:</strong> Activo
              </p>
              <div style="background: #e3f2fd; padding: 20px; border-radius: 8px; margin: 20px 0;">
                <p style="margin: 0; color: #1976d2; font-weight: bold;">
                  🎉 Ya puedes iniciar sesión en la aplicación y comenzar a usar todos los servicios de NexoGo.
                </p>
              </div>
              <p style="font-size: 14px; color: #666;">
                Si tienes alguna pregunta, no dudes en contactarnos.
              </p>
            </div>
            <div style="background: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #666;">
              <p>© 2024 NexoGo - Clínica Veterinaria</p>
            </div>
          </div>
        `,
      });

      // Enviar notificación push
      if (token) {
        await admin.messaging().send({
          token: token,
          notification: {
            title: "Cuenta aprobada 🎉",
            body: "Tu cuenta NexoGo ya está activa. Toca aquí para iniciar sesión.",
          },
          data: {
            type: "user_approved",
            userEmail: email,
            userName: userName || "Usuario",
          },
        });
      }

      console.log(`Usuario aprobado: ${email}`);
      return { success: true, message: "Notificación enviada exitosamente" };
    } catch (error) {
      console.error("Error al enviar notificación:", error);
      throw new functions.https.HttpsError(
        "internal",
        "Error al enviar notificación: " + error.message
      );
    }
  });

// Función para notificar rechazo de usuario
exports.notifyUserRejection = functions
  .region("us-central1")
  .https.onCall(async (data, context) => {
    try {
      const { email, token, userName, reason } = data;

      if (!email) {
        throw new functions.https.HttpsError(
          "invalid-argument",
          "Email es requerido"
        );
      }

      // Enviar correo de rechazo
      await gmailTransport.sendMail({
        from: "NexoGo <tu_correo@gmail.com>",
        to: email,
        subject: "❌ Solicitud de cuenta rechazada",
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <div style="background: linear-gradient(135deg, #d32f2f, #f44336); padding: 20px; text-align: center;">
              <h1 style="color: white; margin: 0;">🐾 NexoGo</h1>
              <p style="color: white; margin: 10px 0 0 0;">Clínica Veterinaria</p>
            </div>
            <div style="padding: 30px; background: #f8f9fa;">
              <h2 style="color: #d32f2f;">Solicitud de cuenta rechazada</h2>
              <p style="font-size: 16px; line-height: 1.6; color: #333;">
                Hola ${userName || "Usuario"},
              </p>
              <p style="font-size: 16px; line-height: 1.6; color: #333;">
                Lamentamos informarte que tu solicitud de cuenta en NexoGo ha sido rechazada.
              </p>
              ${reason ? `
                <div style="background: #ffebee; padding: 15px; border-radius: 8px; margin: 20px 0;">
                  <p style="margin: 0; color: #d32f2f; font-weight: bold;">Razón:</p>
                  <p style="margin: 5px 0 0 0; color: #333;">${reason}</p>
                </div>
              ` : ""}
              <p style="font-size: 16px; line-height: 1.6; color: #333;">
                Si crees que esto es un error o tienes preguntas, puedes contactarnos para más información.
              </p>
            </div>
            <div style="background: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #666;">
              <p>© 2024 NexoGo - Clínica Veterinaria</p>
            </div>
          </div>
        `,
      });

      // Enviar notificación push si hay token
      if (token) {
        await admin.messaging().send({
          token: token,
          notification: {
            title: "Solicitud rechazada",
            body: "Tu solicitud de cuenta fue rechazada. Contacta soporte para más información.",
          },
          data: {
            type: "user_rejected",
            userEmail: email,
            userName: userName || "Usuario",
          },
        });
      }

      console.log(`Usuario rechazado: ${email}`);
      return { success: true, message: "Notificación de rechazo enviada" };
    } catch (error) {
      console.error("Error al enviar notificación de rechazo:", error);
      throw new functions.https.HttpsError(
        "internal",
        "Error al enviar notificación: " + error.message
      );
    }
  });

// Función para enviar notificación de recordatorio de cita
exports.sendAppointmentReminder = functions
  .region("us-central1")
  .https.onCall(async (data, context) => {
    try {
      const { userToken, appointmentData } = data;

      if (!userToken || !appointmentData) {
        throw new functions.https.HttpsError(
          "invalid-argument",
          "Token y datos de cita son requeridos"
        );
      }

      await admin.messaging().send({
        token: userToken,
        notification: {
          title: "Recordatorio de cita 🐾",
          body: `Tienes una cita programada para ${appointmentData.patientName} el ${appointmentData.date}`,
        },
        data: {
          type: "appointment_reminder",
          appointmentId: appointmentData.id,
          patientName: appointmentData.patientName,
        },
      });

      return { success: true };
    } catch (error) {
      console.error("Error al enviar recordatorio:", error);
      throw new functions.https.HttpsError(
        "internal",
        "Error al enviar recordatorio: " + error.message
      );
    }
  });

// Función auxiliar para obtener el nombre del rol
function getRoleDisplayName(role) {
  const roleNames = {
    VET: "Médico Veterinario",
    VET_ASSISTANT: "Auxiliar Veterinario",
    ASSISTANT: "Asistente",
    ADMIN: "Administrador",
    PATIENT: "Propietario de Mascota",
  };
  return roleNames[role] || role;
}