const express = require('express');
const router = express.Router();
const pokemonController = require('../controllers/pokemonController');

router.post('/register', pokemonController.registerPokemon);
router.get('/register/all', pokemonController.getAllPokemons);
router.get('/user/:user_login', pokemonController.getUserPokemons);
router.get('/registered/all', pokemonController.checkRegisteredPokemons);
router.get('/stats', pokemonController.getHomeStats);
router.get('/search/type/:type', pokemonController.searchByType);
router.get('/search/ability/:ability', pokemonController.searchByAbility);
router.get('/details/:id', pokemonController.getPokemonDetails);
router.put('/:id', pokemonController.updatePokemon);
router.delete('/:id', pokemonController.deletePokemon);

module.exports = router;