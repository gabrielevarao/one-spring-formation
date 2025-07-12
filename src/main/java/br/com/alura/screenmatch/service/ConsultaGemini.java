package br.com.alura.screenmatch.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class ConsultaGemini {

    public static String obterTraducao(String texto){
        Client client = Client.builder().apiKey("AIzaSyB6OHOJCFQVTKHbCYCoHNOwUDfOuOD8vIc").build();
        String prompt = "Traduza para PT-BR a sinopse: " + texto;

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.0-flash-lite-001",
                        prompt,
                        null
                );

        return response.text();
    }
}