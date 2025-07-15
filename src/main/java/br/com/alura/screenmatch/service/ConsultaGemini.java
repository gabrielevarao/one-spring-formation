package br.com.alura.screenmatch.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import io.github.cdimascio.dotenv.Dotenv;

public class ConsultaGemini {

    Dotenv dotenv = Dotenv.configure().load();
    Client client = Client.builder().apiKey(dotenv.get("GEMINI_API_KEY")).build();

    public String obterTraducao(String texto){

        String prompt = "Traduza o seguinte texto para português. Responda apenas com a tradução, sem explicações ou alternativas, em no máximo 250 caracteres. " + texto;

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.0-flash-lite-001",
                        prompt,
                        null);

        return response.text();
    }

}